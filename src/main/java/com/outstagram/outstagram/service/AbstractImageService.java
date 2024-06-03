package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public abstract class AbstractImageService implements ImageService{

    private final ImageMapper imageMapper;

    @Transactional
    @Override
    public void saveImages(List<MultipartFile> imgFiles, Long postId) {
        List<ImageDTO> imageDTOList = new ArrayList<>();

        try {
            for (MultipartFile img : imgFiles) {
                String originName = img.getOriginalFilename();

                // 이미지 (로컬 or s3)에 저장
                String savedName = uploadImage(img);

                imageDTOList.add(
                    ImageDTO.builder()
                        .postId(postId)
                        .originalImgName(originName)
                        .imgUrl(savedName)
                        .createDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build()
                );
            }

            // DB에 이미지 정보들 저장
            imageMapper.insertImages(imageDTOList);

        } catch (Exception e) {
            // DB 저장 실패시 업로드된 이미지 S3에서 삭제
            deleteRealImages(imageDTOList);
            throw new ApiException(e, ErrorCode.SAVE_IMAGE_ERROR);
        }
    }

    @Override
    public List<ImageDTO> getImageInfos(Long postId) {
        return imageMapper.findImagesByPostId(postId);
    }

    @Override
    public List<ImageDTO> getDeletedImages() {
        return imageMapper.findDeletedImages();
    }

    @Transactional
    @Override
    public void softDeleteByIds(List<Long> deleteImgIds) {
        int result = imageMapper.deleteByIds(deleteImgIds);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "이미지 삭제에 실패했습니다.");
        }
    }

    @Override
    public void hardDeleteByIds(List<ImageDTO> deletedImages) {
        List<Long> deletedImageIds = deletedImages.stream()
            .map(ImageDTO::getId)
            .collect(Collectors.toList());

        int result = imageMapper.hardDeleteByIds(deletedImageIds);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "image hard delete 하다가 에러 발생!");
        }
    }

    public abstract String uploadImage(MultipartFile image);

    /**
     * 실제 이미지 파일을 (로컬 or s3)에서 삭제
     */
    public abstract void deleteRealImages(List<ImageDTO> deletedImages);

}
