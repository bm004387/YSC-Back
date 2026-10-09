package com.buc.ysc.community.vo;

import java.util.List;

public record CommunityPost(
        Long postSeq, String authorId, String authorName, String content, String createdAt,
        long likeCount, long commentCount, boolean likedByMe, boolean savedByMe,
        List<CommunityMedia> media
) {
    public record CommunityMedia(Long filSeq, String mediaType, String contentType) {}
}
