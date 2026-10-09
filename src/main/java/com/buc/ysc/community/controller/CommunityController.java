package com.buc.ysc.community.controller;

import com.buc.ysc.community.service.CommunityService;
import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.mapper.CommunityMapper;
import com.buc.ysc.file.service.FileStorageService;
import com.buc.ysc.file.vo.StoredFile;
import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService service;
    private final SessionManager sessions;
    private final CommunityMapper mapper;
    private final FileStorageService fileStorage;

    public CommunityController(CommunityService service, SessionManager sessions, CommunityMapper mapper, FileStorageService fileStorage) {
        this.service = service;
        this.sessions = sessions;
        this.mapper = mapper;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/feed")
    public List<CommunityPost> feed(HttpServletRequest request,
            @RequestParam(defaultValue = "recommended") String type,
            @RequestParam(defaultValue = "20") int limit) {
        return service.feed(session(request).usrId(), type, limit);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Long> create(HttpServletRequest request, @RequestPart("content") String content,
            @RequestPart(value = "visibility", required = false) String visibility,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return Map.of("postSeq", service.create(session(request).usrId(), content, visibility, files));
    }

    @PostMapping("/posts/{postSeq}/seen")
    public Map<String, Boolean> seen(HttpServletRequest request, @PathVariable Long postSeq) {
        service.markSeen(postSeq, session(request).usrId());
        return Map.of("success", true);
    }

    @PutMapping("/posts/{postSeq}/like")
    public Map<String, Boolean> like(HttpServletRequest request, @PathVariable Long postSeq, @RequestBody Toggle body) {
        service.like(postSeq, session(request).usrId(), body.enabled());
        return Map.of("success", true);
    }

    @PutMapping("/posts/{postSeq}/save")
    public Map<String, Boolean> save(HttpServletRequest request, @PathVariable Long postSeq, @RequestBody Toggle body) {
        service.save(postSeq, session(request).usrId(), body.enabled());
        return Map.of("success", true);
    }

    @GetMapping("/files/{filSeq}")
    public ResponseEntity<Resource> file(HttpServletRequest request, @PathVariable Long filSeq) {
        UserSession user = session(request);
        if (mapper.canReadFile(filSeq, user.usrId()) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        StoredFile file = fileStorage.metadata(filSeq);
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.parseMediaType(file.getContTyp()))
                .header("Cache-Control", "private, max-age=3600")
                .body((Resource) new FileSystemResource(fileStorage.pathOf(file)));
    }

    private UserSession session(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        UserSession user = auth != null && auth.startsWith("Bearer ") ? sessions.getSession(auth.substring(7)) : null;
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return user;
    }
    public record Toggle(boolean enabled) {}
}
