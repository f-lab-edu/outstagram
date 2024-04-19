package com.outstagram.outstagram.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    void saveImages(List<MultipartFile> imgFiles, Long postId);

}
