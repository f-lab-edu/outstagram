package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    void saveImages(List<MultipartFile> imgFiles, Long postId);

    List<ImageDTO> getImages(Long postId);

    List<ImageDTO> getDeletedImages();

    /**
     * DB의 image 테이블에서 해당 레코드의 is_delete = 1로 수정(soft delete)
     */
    void deleteByIds(List<Long> deleteImgIds);

    /**
     * 실제 이미지 파일 삭제(hard delete)
     */
    void deleteLocalImages(List<ImageDTO> deletedImages);

    void hardDeleteByIds(List<ImageDTO> deletedImages);
}
