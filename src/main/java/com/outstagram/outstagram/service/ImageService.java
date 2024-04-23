package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    void saveImages(List<MultipartFile> imgFiles, Long postId);

    ImageDTO getFirstImage(Long postId);

    List<ImageDTO> getImages(Long postId);

    void deleteByIds(List<Long> deleteImgIds);
}
