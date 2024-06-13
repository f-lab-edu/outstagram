package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.ImageDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMapper {

    void insertImages(List<ImageDTO> imageList);

    List<ImageDTO> findImagesByPostId(Long postId);

    List<ImageDTO> findDeletedImages();

    /**
     * soft delete 방식으로, is_deleted = 1로 업데이트함으로써, 삭제됨을 표현
     */
    int deleteByIds(List<Long> deleteImgIds);

    int deleteByPostId(Long postId);

    int hardDeleteByIds(List<Long> deleteImgIds);
}
