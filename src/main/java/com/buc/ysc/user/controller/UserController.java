package com.buc.ysc.user.controller;

import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import com.buc.ysc.file.service.FileStorageService;
import com.buc.ysc.file.vo.StoredFile;
import com.buc.ysc.user.mapper.UserMapper;
import com.buc.ysc.user.vo.request.AddressChangeRequest;
import com.buc.ysc.user.vo.request.PasswordChangeRequest;
import com.buc.ysc.user.vo.request.UserVO;
import com.buc.ysc.user.vo.response.UserProfileResponse;
import com.buc.ysc.util.MsgUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final SessionManager sessionManager;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final MsgUtil msgUtil;

    public UserController(UserMapper userMapper, SessionManager sessionManager, PasswordEncoder passwordEncoder,
                          FileStorageService fileStorageService, MsgUtil msgUtil) {
        this.userMapper = userMapper;
        this.sessionManager = sessionManager;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.msgUtil = msgUtil;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(HttpServletRequest request) {
        String token = tokenFrom(request);
        UserSession session = requireSession(token);
        UserVO user = userMapper.selectByUsrId(session.usrId());
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, msgUtil.getMsg("MYINFO", "010"));
        return new UserProfileResponse(session.usrId(), session.usrNm(),
                valueOrDatabase(session.hpNo(), user.getHpNo()),
                valueOrDatabase(session.adr(), user.getAdr()),
                valueOrDatabase(session.dtlAdr(), user.getDtlAdr()),
                user.getPrflImgFilSeq() == null ? null : "/api/user/me/profile-image");
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody PasswordChangeRequest body) {
        UserSession session = requireSession(tokenFrom(request));
        UserVO user = userMapper.selectByUsrId(session.usrId());
        if (user == null || body.currentPassword() == null || !passwordEncoder.matches(body.currentPassword(), user.getPwd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msgUtil.getMsg("MYINFO", "001"));
        }
        if (body.newPassword() == null || body.newPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msgUtil.getMsg("MYINFO", "002"));
        }
        if (!body.newPassword().equals(body.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msgUtil.getMsg("COMMON", "003"));
        }
        userMapper.updatePassword(session.usrId(), passwordEncoder.encode(body.newPassword()));
        return ResponseEntity.ok(java.util.Map.of("message", msgUtil.getMsg("MYINFO", "003")));
    }

    @PutMapping("/me/address")
    public ResponseEntity<?> changeAddress(HttpServletRequest request, @RequestBody AddressChangeRequest body) {
        String token = tokenFrom(request);
        UserSession session = requireSession(token);
        if (body.adr() == null || body.adr().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msgUtil.getMsg("COMMON", "007"));
        }
        userMapper.updateAddress(session.usrId(), body.adr().trim(), body.dtlAdr() == null ? "" : body.dtlAdr().trim());
        sessionManager.updateAddress(token, body.adr().trim(), body.dtlAdr() == null ? "" : body.dtlAdr().trim());
        return ResponseEntity.ok(java.util.Map.of("message", msgUtil.getMsg("MYINFO", "004")));
    }

    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> saveProfileImage(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
        String token = tokenFrom(request);
        UserSession session = requireSession(token);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msgUtil.getMsg("MYINFO", "011"));
        }
        UserVO user = userMapper.selectByUsrId(session.usrId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msgUtil.getMsg("MYINFO", "010"));
        }
        Long newFileSeq = fileStorageService.store(file, "USER", "PROFILE", session.usrId());
        int updatedRows = userMapper.updateProfileImageFileSeq(session.usrId(), newFileSeq);
        if (updatedRows != 1) {
            fileStorageService.delete(newFileSeq);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msgUtil.getMsg("MYINFO", "010"));
        }
        return ResponseEntity.ok(java.util.Map.of(
                "message", msgUtil.getMsg("MYINFO", "005"),
                "profileImageUrl", "/api/user/me/profile-image"));
    }

    @GetMapping("/me/profile-image")
    public ResponseEntity<Resource> getProfileImage(HttpServletRequest request) {
        UserSession session = requireSession(tokenFrom(request));
        UserVO user = userMapper.selectByUsrId(session.usrId());
        if (user == null || user.getPrflImgFilSeq() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msgUtil.getMsg("MYINFO", "013"));
        }
        StoredFile file = fileStorageService.metadata(user.getPrflImgFilSeq());
        MediaType mediaType;
        try { mediaType = MediaType.parseMediaType(file.getContTyp()); }
        catch (RuntimeException invalidType) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
                .body((Resource) new FileSystemResource(fileStorageService.pathOf(file)));
    }

    private UserSession requireSession(String token) {
        UserSession session = sessionManager.getSession(token);
        if (session == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("MYINFO", "009"));
        return session;
    }

    private String tokenFrom(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msgUtil.getMsg("MYINFO", "009"));
        }
        return authorization.substring(7);
    }

    private String valueOrDatabase(String sessionValue, String databaseValue) {
        return sessionValue == null || sessionValue.isBlank() ? databaseValue : sessionValue;
    }
}
