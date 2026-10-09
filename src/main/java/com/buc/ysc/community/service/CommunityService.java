package com.buc.ysc.community.service;

import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.vo.CommunityPostCommandVO;
import java.util.List;

/** 커뮤니티 업무 처리 기능을 정의합니다. */
public interface CommunityService {

    /** 피드 종류에 맞는 게시물과 반응 정보를 조회합니다. */
    List<CommunityPost> feed(String userId, String feedType, int limit);

    /** 사용자가 볼 수 있는 게시물의 댓글을 조회합니다. */
    List<CommunityPost.CommunityComment> comments(Long postSeq, String userId);

    /** 게시물과 첨부 파일을 저장합니다. */
    Long create(CommunityPostCommandVO command);

    /** 게시물의 조회 완료 기록을 저장합니다. */
    void markSeen(CommunityPostCommandVO command);

    /** 게시물 좋아요 상태를 변경합니다. */
    void like(CommunityPostCommandVO command);

    /** 게시물 저장 상태를 변경합니다. */
    void save(CommunityPostCommandVO command);
}
