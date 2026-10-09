package com.buc.ysc.file.mapper;

import com.buc.ysc.file.vo.StoredFile;
import org.apache.ibatis.annotations.Param;

public interface FileMapper {
    Long nextFileSeq();

    int insertFileBase(StoredFile file);

    int insertFileDetail(StoredFile file);

    StoredFile selectFile(@Param("filSeq") Long filSeq);

    int deleteFileDetails(@Param("filSeq") Long filSeq);

    int deleteFileBase(@Param("filSeq") Long filSeq);
}
