package com.buc.ysc.community.mapper;

import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.vo.CommunityPostRowVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 커뮤니티 SQL 매퍼 인터페이스입니다. */
public interface CommunityMapper {

    /** 게시물 일련번호를 생성합니다. */
    Long nextPostSeq();

    /** 게시물 본문을 저장합니다. */
    int insertPost(
            @Param("postSeq") Long postSeq,
            @Param("usrId") String usrId,
            @Param("title") String title,
            @Param("content") String content,
            @Param("visibility") String visibility);

    /** 게시물과 파일의 연결 정보를 저장합니다. */
    int insertPostFile(
            @Param("postSeq") Long postSeq,
            @Param("filSeq") Long filSeq,
            @Param("sortOrder") int sortOrder,
            @Param("mediaType") String mediaType,
            @Param("usrId") String usrId);

    /** 사용자와 피드 종류에 해당하는 게시물을 조회합니다. */
    List<CommunityPostRowVO> selectFeed(
            @Param("usrId") String usrId,
            @Param("feedType") String feedType,
            @Param("limit") int limit);

    /** 게시물에 연결된 미디어 정보를 조회합니다. */
    List<CommunityPost.CommunityMedia> selectMedia(@Param("postSeq") Long postSeq);

    /** 사용자별 게시물 조회 기록을 저장합니다. */
    int markSeen(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 게시물 좋아요를 추가합니다. */
    int addLike(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 게시물 좋아요를 삭제합니다. */
    int removeLike(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 게시물 저장 표시를 추가합니다. */
    int addSave(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 게시물 저장 표시를 삭제합니다. */
    int removeSave(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 사용자가 첨부 파일을 조회할 수 있는지 검사합니다. */
    int canReadFile(@Param("filSeq") Long filSeq, @Param("usrId") String usrId);
}
