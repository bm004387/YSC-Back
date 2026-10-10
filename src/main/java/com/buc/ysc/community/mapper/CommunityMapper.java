package com.buc.ysc.community.mapper;

import com.buc.ysc.community.vo.CommunityPost;
import com.buc.ysc.community.vo.CommunityPostCommandVO;
import com.buc.ysc.community.vo.CommunityPostRowVO;
import com.buc.ysc.community.vo.CommunityProfileSummaryVO;
import com.buc.ysc.community.vo.CommunityCommentPreviewVO;
import com.buc.ysc.community.vo.CommunityUserProfileVO;
import com.buc.ysc.community.vo.CommunityUserSearchVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 커뮤니티 SQL 매퍼 인터페이스입니다. */
public interface CommunityMapper {

    /** 내 게시물·팔로워·팔로잉 수를 조회합니다. */
    CommunityProfileSummaryVO selectProfileSummary(@Param("usrId") String usrId);

    /** 사용자 프로필과 로그인 사용자의 팔로우 상태를 조회합니다. */
    CommunityUserProfileVO selectUserProfile(
            @Param("usrId") String usrId,
            @Param("viewerUsrId") String viewerUsrId);

    /** 팔로우 대상 사용자가 존재하는지 확인합니다. */
    int countUser(@Param("usrId") String usrId);

    /** 사용자의 게시물 피드에 팔로우 사용자 조건을 전달합니다. */
    int followUser(@Param("usrId") String usrId, @Param("followingUsrId") String followingUsrId);

    /** 팔로우 관계를 해제합니다. */
    int unfollowUser(@Param("usrId") String usrId, @Param("followingUsrId") String followingUsrId);

    /** 지정한 팔로워와 현재 사용자의 팔로우 관계를 제거합니다. */
    int removeFollower(
            @Param("usrId") String usrId,
            @Param("followerUsrId") String followerUsrId);

    /** 검색어와 관계 필터에 맞는 계정을 조회합니다. */
    List<CommunityUserSearchVO> searchUsers(
            @Param("usrId") String usrId,
            @Param("query") String query,
            @Param("userFilter") String userFilter,
            @Param("limit") int limit);

    /** 로그인 사용자의 팔로워 또는 팔로잉 목록을 조회합니다. */
    List<CommunityUserSearchVO> selectFollowUsers(
            @Param("usrId") String usrId,
            @Param("relationType") String relationType,
            @Param("limit") int limit);

    /** 공개 범위에 맞는 게시물 중 본문 검색 결과를 조회합니다. */
    List<CommunityPostRowVO> searchPosts(
            @Param("usrId") String usrId,
            @Param("query") String query,
            @Param("limit") int limit);

    /** 여러 게시물 각각의 최신 댓글을 두 건까지 조회합니다. */
    List<CommunityCommentPreviewVO> selectCommentPreviews(
            @Param("usrId") String usrId,
            @Param("postSeqs") List<Long> postSeqs);

    /** 게시물 일련번호를 생성합니다. */
    Long nextPostSeq();

    /** 게시물 본문을 저장합니다. */
    int insertPost(
            @Param("postSeq") Long postSeq,
            @Param("command") CommunityPostCommandVO command);

    /** 게시물과 파일의 연결 정보를 저장합니다. */
    int insertPostFile(
            @Param("postSeq") Long postSeq,
            @Param("filSeq") Long filSeq,
            @Param("sortOrder") int sortOrder,
            @Param("mediaType") String mediaType,
            @Param("command") CommunityPostCommandVO command);

    /** 사용자와 피드 종류에 해당하는 게시물을 조회합니다. */
    List<CommunityPostRowVO> selectFeed(
            @Param("usrId") String usrId,
            @Param("feedType") String feedType,
            @Param("limit") int limit,
            @Param("profileUsrId") String profileUsrId);

    /** 게시물에 연결된 미디어 정보를 조회합니다. */
    List<CommunityPost.CommunityMedia> selectMedia(@Param("postSeq") Long postSeq);

    /** 게시물 조회 권한을 확인합니다. */
    int canReadPost(@Param("postSeq") Long postSeq, @Param("usrId") String usrId);

    /** 활성 댓글을 시간순으로 조회합니다. */
    List<CommunityPost.CommunityComment> selectComments(@Param("postSeq") Long postSeq);

    /** 게시물에 속한 활성 부모 댓글인지 확인합니다. */
    int countActiveComment(
            @Param("postSeq") Long postSeq,
            @Param("cmtSeq") Long cmtSeq);

    /** 댓글 내용을 새로 저장합니다. */
    int insertComment(@Param("command") CommunityPostCommandVO command);

    /** 작성자 본인의 활성 댓글을 수정합니다. */
    int updateComment(@Param("command") CommunityPostCommandVO command);

    /** 작성자 본인의 활성 댓글을 삭제 상태로 변경합니다. */
    int deleteComment(@Param("command") CommunityPostCommandVO command);

    /** 사용자별 게시물 조회 기록을 저장합니다. */
    int markSeen(@Param("command") CommunityPostCommandVO command);

    /** 게시물 좋아요를 추가합니다. */
    int addLike(@Param("command") CommunityPostCommandVO command);

    /** 게시물 좋아요를 삭제합니다. */
    int removeLike(@Param("command") CommunityPostCommandVO command);

    /** 게시물 저장 표시를 추가합니다. */
    int addSave(@Param("command") CommunityPostCommandVO command);

    /** 게시물 저장 표시를 삭제합니다. */
    int removeSave(@Param("command") CommunityPostCommandVO command);

    /** 사용자가 첨부 파일을 조회할 수 있는지 검사합니다. */
    int canReadFile(@Param("filSeq") Long filSeq, @Param("usrId") String usrId);
}
