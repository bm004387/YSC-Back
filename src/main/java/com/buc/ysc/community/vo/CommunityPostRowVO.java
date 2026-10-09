package com.buc.ysc.community.vo;

/** 게시물 조회 SQL의 기본 컬럼을 담는 MyBatis 매핑용 VO입니다. */
public record CommunityPostRowVO(
        Long postSeq,
        String authorId,
        String authorName,
        String content,
        String createdAt,
        long likeCount,
        long commentCount,
        boolean likedByMe,
        boolean savedByMe
) {
}
