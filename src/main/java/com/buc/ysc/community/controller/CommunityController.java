package com.buc.ysc.community.controller;

import com.buc.ysc.community.mapper.CommunityMapper;
import com.buc.ysc.community.service.CommunityService;
import com.buc.ysc.community.vo.CommunityPostCommandVO;
import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.file.service.FileStorageService;
import com.buc.ysc.file.vo.StoredFile;
import com.buc.ysc.security.SessionManager;
import com.buc.ysc.security.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** 커뮤니티 피드와 게시물 요청을 처리하는 REST 컨트롤러입니다. */
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService service;
    private final SessionManager sessions;
    private final CommunityMapper mapper;
    private final FileStorageService fileStorage;

    public CommunityController(
            CommunityService service,
            SessionManager sessions,
            CommunityMapper mapper,
            FileStorageService fileStorage) {
        this.service = service;
        this.sessions = sessions;
        this.mapper = mapper;
        this.fileStorage = fileStorage;
    }

    /** 선택한 피드 종류의 게시물을 조회합니다. */
    @GetMapping("/feed")
    public List<CommunityPost> feed(
            HttpServletRequest request,
            @RequestParam(defaultValue = "recommended") String type,
            @RequestParam(defaultValue = "20") int limit) {
        return service.feed(session(request).usrId(), type, limit);
    }

    /** 게시물의 댓글 목록을 반환합니다. */
    @GetMapping("/posts/{postSeq}/comments")
    public List<CommunityPost.CommunityComment> comments(
            HttpServletRequest request,
            @PathVariable Long postSeq) {
        return service.comments(postSeq, session(request).usrId());
    }

    /** 로그인 사용자의 댓글을 게시물에 등록합니다. */
    @PostMapping("/posts/{postSeq}/comments")
    public Map<String, Boolean> addComment(
            HttpServletRequest request,
            @PathVariable Long postSeq,
            @RequestBody CommentRequest body) {
        CommunityPostCommandVO command = commandFor(session(request));
        command.setPostSeq(postSeq);
        command.setCommentContent(body.content());
        command.setParentCmtSeq(body.parentCmtSeq());
        service.addComment(command);
        return Map.of("success", true);
    }

    /** 본문과 첨부 파일을 새 게시물로 등록합니다. */
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Long> create(
            HttpServletRequest request,
            @RequestPart("content") String content,
            @RequestPart(value = "visibility", required = false) String visibility,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        UserSession session = session(request);
        CommunityPostCommandVO command = commandFor(session);
        command.setContent(content);
        command.setVisibility(visibility);
        command.setFiles(files);
        return Map.of("postSeq", service.create(command));
    }

    /** 사용자가 피드에서 확인한 게시물을 읽음 처리합니다. */
    @PostMapping("/posts/{postSeq}/seen")
    public Map<String, Boolean> seen(
            HttpServletRequest request,
            @PathVariable Long postSeq) {
        CommunityPostCommandVO command = commandFor(session(request));
        command.setPostSeq(postSeq);
        service.markSeen(command);
        return Map.of("success", true);
    }

    /** 게시물 좋아요 상태를 변경합니다. */
    @PutMapping("/posts/{postSeq}/like")
    public Map<String, Boolean> like(
            HttpServletRequest request,
            @PathVariable Long postSeq,
            @RequestBody Toggle body) {
        CommunityPostCommandVO command = commandFor(session(request));
        command.setPostSeq(postSeq);
        command.setEnabled(body.enabled());
        service.like(command);
        return Map.of("success", true);
    }

    /** 게시물 저장 상태를 변경합니다. */
    @PutMapping("/posts/{postSeq}/save")
    public Map<String, Boolean> save(
            HttpServletRequest request,
            @PathVariable Long postSeq,
            @RequestBody Toggle body) {
        CommunityPostCommandVO command = commandFor(session(request));
        command.setPostSeq(postSeq);
        command.setEnabled(body.enabled());
        service.save(command);
        return Map.of("success", true);
    }

    /** 공개 범위를 확인한 뒤 게시물 첨부 파일을 반환합니다. */
    @GetMapping("/files/{filSeq}")
    public ResponseEntity<Resource> file(
            HttpServletRequest request,
            @PathVariable Long filSeq) {
        UserSession user = session(request);
        if (mapper.canReadFile(filSeq, user.usrId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        }
        StoredFile file = fileStorage.metadata(filSeq);
        MediaType contentType = MediaType.parseMediaType(file.getContTyp());
        return ResponseEntity.ok()
                .contentType(contentType)
                .header("Cache-Control", "private, max-age=3600")
                .body((Resource) new FileSystemResource(fileStorage.pathOf(file)));
    }

    /** Authorization 헤더의 세션 토큰을 검증합니다. */
    private UserSession session(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        UserSession user = auth != null && auth.startsWith("Bearer ")
                ? sessions.getSession(auth.substring(7))
                : null;
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return user;
    }

    /** 세션 사용자 ID를 업무 VO와 공통 시스템 컬럼에 설정합니다. */
    private CommunityPostCommandVO commandFor(UserSession session) {
        CommunityPostCommandVO command = new CommunityPostCommandVO();
        command.setUsrId(session.usrId());
        command.setSystemUserId(session.usrId());
        return command;
    }

    /** 좋아요·저장 변경 요청 값입니다. */
    public record Toggle(boolean enabled) {
    }

    /** 댓글 등록 요청 값입니다. */
    public record CommentRequest(String content, Long parentCmtSeq) {
    }
}
