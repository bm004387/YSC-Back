package com.buc.ysc.community.service.impl;

import com.buc.ysc.community.mapper.CommunityMapper;
import com.buc.ysc.community.service.CommunityService;
import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.vo.CommunityPostCommandVO;
import com.buc.ysc.community.vo.CommunityPostRowVO;
import com.buc.ysc.community.vo.CommunityProfileSummaryVO;
import com.buc.ysc.community.vo.CommunityCommentPreviewVO;
import com.buc.ysc.community.vo.CommunityUserProfileVO;
import com.buc.ysc.file.service.FileStorageService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** 커뮤니티 업무 규칙과 파일 저장 처리를 구현합니다. */
@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper mapper;
    private final FileStorageService fileStorage;

    public CommunityServiceImpl(CommunityMapper mapper, FileStorageService fileStorage) {
        this.mapper = mapper;
        this.fileStorage = fileStorage;
    }

    /** 로그인 사용자의 게시물·팔로워·팔로잉 수를 조회합니다. */
    @Override
    public CommunityProfileSummaryVO profileSummary(String userId) {
        return mapper.selectProfileSummary(userId);
    }

    /** 대상 사용자 존재 여부를 확인하고 프로필 통계를 조회합니다. */
    @Override
    public CommunityUserProfileVO userProfile(String viewerUsrId, String profileUsrId) {
        CommunityUserProfileVO profile = mapper.selectUserProfile(profileUsrId, viewerUsrId);
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return profile;
    }

    /** 기존 피드 조회 규칙으로 대상 사용자의 공개 가능한 게시물을 조회합니다. */
    @Override
    public List<CommunityPost> userPosts(String viewerUsrId, String profileUsrId) {
        if (mapper.countUser(profileUsrId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return mapPosts(mapper.selectFeed(viewerUsrId, "profile", 50, profileUsrId));
    }

    /** 자기 자신 팔로우를 차단하고 팔로우 상태를 변경합니다. */
    @Override
    @Transactional
    public void setFollow(String usrId, String followingUsrId, boolean enabled) {
        if (usrId.equals(followingUsrId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우할 수 없습니다.");
        }
        if (mapper.countUser(followingUsrId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        if (enabled) {
            mapper.followUser(usrId, followingUsrId);
        } else {
            mapper.unfollowUser(usrId, followingUsrId);
        }
    }

    private List<CommunityPost> mapPosts(List<CommunityPostRowVO> rows) {
        return rows.stream()
                .map(row -> new CommunityPost(
                        row.getPostSeq(), row.getAuthorId(), row.getAuthorName(),
                        row.getProfileImageFilSeq(), row.getContent(), row.getCreatedAt(),
                        row.getLikeCount(), row.getCommentCount(), row.isLikedByMe(),
                        row.isSavedByMe(), mapper.selectMedia(row.getPostSeq())))
                .toList();
    }

    /** 게시물 ID를 확인하고 댓글 미리보기를 일괄 조회합니다. */
    @Override
    public List<CommunityCommentPreviewVO> commentPreviews(String userId, List<Long> postSeqs) {
        if (postSeqs == null || postSeqs.isEmpty()) {
            return List.of();
        }
        if (postSeqs.size() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시물은 한 번에 50개까지 조회할 수 있습니다.");
        }
        return mapper.selectCommentPreviews(userId, postSeqs.stream().distinct().toList());
    }

    /** 피드 결과에 첨부 파일 정보를 합쳐 반환합니다. */
    @Override
    public List<CommunityPost> feed(String userId, String feedType, int limit) {
        String type = List.of("recommended", "following", "popular", "mine", "saved").contains(feedType)
                ? feedType
                : "recommended";
        List<CommunityPostRowVO> rows = mapper.selectFeed(
                userId, type, Math.max(1, Math.min(limit, 50)), userId);
        return mapPosts(rows);
    }

    /** 게시물 공개 권한을 검사한 뒤 활성 댓글을 반환합니다. */
    @Override
    public List<CommunityPost.CommunityComment> comments(Long postSeq, String userId) {
        if (mapper.canReadPost(postSeq, userId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        return mapper.selectComments(postSeq);
    }

    /** 게시물 공개 권한과 내용을 확인한 뒤 댓글을 등록합니다. */
    @Override
    @Transactional
    public void addComment(CommunityPostCommandVO command) {
        if (mapper.canReadPost(command.getPostSeq(), command.getUsrId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        if (command.getCommentContent() == null || command.getCommentContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해 주세요.");
        }
        if (command.getParentCmtSeq() != null
                && mapper.countActiveComment(command.getPostSeq(), command.getParentCmtSeq()) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답글을 달 댓글을 찾을 수 없습니다.");
        }
        command.setCommentContent(command.getCommentContent().trim());
        mapper.insertComment(command);
    }

    /** 로그인 사용자가 작성한 활성 댓글만 수정합니다. */
    @Override
    @Transactional
    public void updateComment(CommunityPostCommandVO command) {
        if (mapper.canReadPost(command.getPostSeq(), command.getUsrId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        if (command.getCommentContent() == null || command.getCommentContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해 주세요.");
        }
        command.setCommentContent(command.getCommentContent().trim());
        if (mapper.updateComment(command) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "본인이 작성한 댓글만 수정할 수 있습니다.");
        }
    }

    /** 로그인 사용자가 작성한 댓글만 삭제 상태로 변경합니다. */
    @Override
    @Transactional
    public void deleteComment(CommunityPostCommandVO command) {
        if (mapper.canReadPost(command.getPostSeq(), command.getUsrId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        if (mapper.deleteComment(command) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
    }

    /** 게시물과 업로드된 미디어를 트랜잭션으로 저장합니다. */
    @Override
    @Transactional
    public Long create(CommunityPostCommandVO command) {
        String userId = command.getUsrId();
        String content = command.getContent();
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시물 내용을 입력해 주세요.");
        }
        String vis = "FOLLOWER".equalsIgnoreCase(command.getVisibility()) ? "FOLLOWER" : "PUBLIC";
        Long seq = mapper.nextPostSeq();
        command.setContent(content.trim());
        command.setVisibility(vis);
        mapper.insertPost(seq, command);
        List<Long> stored = new ArrayList<>();
        try {
            int order = 1;
            for (MultipartFile file : command.getFiles() == null
                    ? List.<MultipartFile>of()
                    : command.getFiles()) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String contentType = file.getContentType();
                String type = contentType != null && contentType.toLowerCase().startsWith("video/")
                        ? "VIDEO"
                        : "IMAGE";
                Long filSeq = fileStorage.store(file, "COMM", "1", userId);
                stored.add(filSeq);
                mapper.insertPostFile(seq, filSeq, order++, type, command);
            }
        } catch (RuntimeException ex) {
            stored.forEach(fileStorage::delete);
            throw ex;
        }
        return seq;
    }

    /** 게시물 열람 기록을 저장합니다. */
    @Override
    @Transactional
    public void markSeen(CommunityPostCommandVO command) {
        mapper.markSeen(command);
    }

    /** 게시물 좋아요를 등록하거나 취소합니다. */
    @Override
    @Transactional
    public void like(CommunityPostCommandVO command) {
        if (Boolean.TRUE.equals(command.getEnabled())) {
            mapper.addLike(command);
        } else {
            mapper.removeLike(command);
        }
    }

    /** 게시물 저장 표시를 등록하거나 취소합니다. */
    @Override
    @Transactional
    public void save(CommunityPostCommandVO command) {
        if (Boolean.TRUE.equals(command.getEnabled())) {
            mapper.addSave(command);
        } else {
            mapper.removeSave(command);
        }
    }
}
