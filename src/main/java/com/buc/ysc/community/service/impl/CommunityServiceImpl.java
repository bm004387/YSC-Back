package com.buc.ysc.community.service.impl;

import com.buc.ysc.community.mapper.CommunityMapper;
import com.buc.ysc.community.service.CommunityService;
import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.file.service.FileStorageService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityServiceImpl implements CommunityService {
    private final CommunityMapper mapper;
    private final FileStorageService fileStorage;

    public CommunityServiceImpl(CommunityMapper mapper, FileStorageService fileStorage) {
        this.mapper = mapper;
        this.fileStorage = fileStorage;
    }

    @Override
    public List<CommunityPost> feed(String userId, String feedType, int limit) {
        String type = List.of("recommended", "following", "popular").contains(feedType) ? feedType : "recommended";
        List<CommunityPost> posts = mapper.selectFeed(userId, type, Math.max(1, Math.min(limit, 50)));
        return posts.stream().map(p -> new CommunityPost(p.postSeq(), p.authorId(), p.authorName(), p.content(),
                p.createdAt(), p.likeCount(), p.commentCount(), p.likedByMe(), p.savedByMe(),
                mapper.selectMedia(p.postSeq()))).toList();
    }

    @Override
    @Transactional
    public Long create(String userId, String content, String visibility, List<MultipartFile> files) {
        if (content == null || content.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시물 내용을 입력해 주세요.");
        String vis = "FOLLOWER".equalsIgnoreCase(visibility) ? "FOLLOWER" : "PUBLIC";
        Long seq = mapper.nextPostSeq();
        mapper.insertPost(seq, userId, null, content.trim(), vis);
        List<Long> stored = new ArrayList<>();
        try {
            int order = 1;
            for (MultipartFile file : files == null ? List.<MultipartFile>of() : files) {
                if (file == null || file.isEmpty()) continue;
                String type = file.getContentType() != null && file.getContentType().toLowerCase().startsWith("video/") ? "VIDEO" : "IMAGE";
                Long filSeq = fileStorage.store(file, "COMM", "1", userId);
                stored.add(filSeq);
                mapper.insertPostFile(seq, filSeq, order++, type, userId);
            }
        } catch (RuntimeException ex) {
            stored.forEach(fileStorage::delete);
            throw ex;
        }
        return seq;
    }

    @Override @Transactional public void markSeen(Long postSeq, String userId) { mapper.markSeen(postSeq, userId); }
    @Override @Transactional public void like(Long postSeq, String userId, boolean enabled) {
        if (enabled) mapper.addLike(postSeq, userId); else mapper.removeLike(postSeq, userId);
    }
    @Override @Transactional public void save(Long postSeq, String userId, boolean enabled) {
        if (enabled) mapper.addSave(postSeq, userId); else mapper.removeSave(postSeq, userId);
    }
}
