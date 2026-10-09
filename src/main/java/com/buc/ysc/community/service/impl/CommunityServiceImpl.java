package com.buc.ysc.community.service.impl;

import com.buc.ysc.community.mapper.CommunityMapper;
import com.buc.ysc.community.service.CommunityService;
import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.vo.CommunityPostCommandVO;
import com.buc.ysc.community.vo.CommunityPostRowVO;
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

    /** 피드 결과에 첨부 파일 정보를 합쳐 반환합니다. */
    @Override
    public List<CommunityPost> feed(String userId, String feedType, int limit) {
        String type = List.of("recommended", "following", "popular", "mine").contains(feedType)
                ? feedType
                : "recommended";
        List<CommunityPostRowVO> rows = mapper.selectFeed(userId, type, Math.max(1, Math.min(limit, 50)));
        return rows.stream()
                .map(row -> new CommunityPost(
                        row.getPostSeq(),
                        row.getAuthorId(),
                        row.getAuthorName(),
                        row.getContent(),
                        row.getCreatedAt(),
                        row.getLikeCount(),
                        row.getCommentCount(),
                        row.isLikedByMe(),
                        row.isSavedByMe(),
                        mapper.selectMedia(row.getPostSeq())))
                .toList();
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
