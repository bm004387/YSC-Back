package com.buc.ysc.community.service;

import com.buc.ysc.community.vo.CommunityPost;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CommunityService {
    List<CommunityPost> feed(String userId, String feedType, int limit);
    Long create(String userId, String content, String visibility, List<MultipartFile> files);
    void markSeen(Long postSeq, String userId);
    void like(Long postSeq, String userId, boolean enabled);
    void save(Long postSeq, String userId, boolean enabled);
}
