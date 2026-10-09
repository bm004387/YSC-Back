package com.buc.ysc.community.mapper;

import com.buc.ysc.community.vo.CommunityPost;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CommunityMapper {
    Long nextPostSeq();
    int insertPost(@Param("postSeq") Long postSeq, @Param("usrId") String usrId,
                   @Param("title") String title, @Param("content") String content,
                   @Param("visibility") String visibility);
    int insertPostFile(@Param("postSeq") Long postSeq, @Param("filSeq") Long filSeq,
                       @Param("sortOrder") int sortOrder, @Param("mediaType") String mediaType,
                       @Param("usrId") String usrId);
    List<CommunityPost> selectFeed(@Param("usrId") String usrId, @Param("feedType") String feedType,
                                  @Param("limit") int limit);
    List<CommunityPost.CommunityMedia> selectMedia(@Param("postSeq") Long postSeq);
    int markSeen(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);
    int addLike(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);
    int removeLike(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);
    int addSave(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);
    int removeSave(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);
    int canReadFile(@Param("filSeq") Long filSeq, @Param("usrId") String usrId);
}
