package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public abstract class AbstractImageService implements ImageService{

    private final ImageMapper imageMapper;

    @Transactional
    @Override
    public void saveImages(List<MultipartFile> imgFiles, Long postId) {
        // image DB에 저장
        List<ImageDTO> imageDTOList = new ArrayList<>();
        List<String> uploadedImagePaths = new ArrayList<>();

        try {
            for (MultipartFile img : imgFiles) {
                String originName = img.getOriginalFilename();

                // 이미지 (로컬 or s3)에 저장
                String savedName = uploadImage(img);
                uploadedImagePaths.add(savedName);

                imageDTOList.add(
                    ImageDTO.builder()
                        .postId(postId)
                        .originalImgName(originName)
                        .savedImgName(savedName)
                        .createDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build()
                );
            }

            // 이미지 정보들 한꺼번에 DB에 저장
            imageMapper.insertImages(imageDTOList);
        } catch (Exception e) {
            // DB 저장 실패시 업로드된 이미지 S3에서 삭제
            for (String imagePath : uploadedImagePaths) {
                deleteImage(imagePath);
            }
            throw new ApiException(ErrorCode.SAVE_IMAGE_ERROR, "이미지 저장에 실패했습니다.", e);
        }
    }

    @Override
    public List<ImageDTO> getImageInfos(Long postId) {
        return imageMapper.findImagesByPostId(postId);
    }

    @Transactional
    @Override
    public void softDeleteByIds(List<Long> deleteImgIds) {
        int result = imageMapper.deleteByIds(deleteImgIds);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "이미지 삭제에 실패했습니다.");
        }
    }

    public abstract String uploadImage(MultipartFile image);
}
