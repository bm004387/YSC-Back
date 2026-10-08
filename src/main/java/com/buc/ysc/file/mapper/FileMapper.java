package com.buc.ysc.file.mapper;

import com.buc.ysc.file.vo.StoredFile;
import org.apache.ibatis.annotations.Param;

public interface FileMapper {
    Long nextFileSeq();

    int insertFileBase(@Param("filSeq") Long filSeq, @Param("filTyp") String filTyp,
                       @Param("filCd") String filCd, @Param("filPth") String filPth,
                       @Param("usrId") String usrId);

    int insertFileDetail(@Param("filSeq") Long filSeq, @Param("filDtlSeq") Long filDtlSeq,
                         @Param("orgFilNm") String orgFilNm, @Param("savFilNm") String savFilNm,
                         @Param("filExt") String filExt, @Param("filSz") Long filSz,
                         @Param("contTyp") String contTyp, @Param("usrId") String usrId);

    StoredFile selectFile(@Param("filSeq") Long filSeq);

    int deleteFileDetails(@Param("filSeq") Long filSeq);

    int deleteFileBase(@Param("filSeq") Long filSeq);
}
