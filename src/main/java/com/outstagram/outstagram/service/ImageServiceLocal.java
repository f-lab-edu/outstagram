package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceLocal implements ImageService {

    @Value("com.outstagram.upload.path")
    private String uploadPath;

    private final ImageMapper imageMapper;

    @PostConstruct
    public void init() {
        File tempFolder = new File(uploadPath);

        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }

        uploadPath = tempFolder.getAbsolutePath();
        log.info("========================================");
        log.info(uploadPath);
    }


    /**
     * 로컬 디렉토리에 이미지 저장하고, 이미지 정보 DB에 저장하기
     */
    @Override
    public void saveImages(List<MultipartFile> imgFiles, Long postId) {
        List<ImageDTO> imageDTOList = new ArrayList<>();

        // 로컬 디렉토리에 이미지 저장 & imageDTO 생성
        for (MultipartFile img : imgFiles) {
            String originName = img.getOriginalFilename();
            String savedName = UUID.randomUUID() + "_" + originName;
            Path savePath = Paths.get(uploadPath, savedName);

            try {
                // 로컬 디렉토리에 이미지 파일 저장
                Files.copy(img.getInputStream(), savePath);

                // ImageDTO 생성 및 리스트에 추가(한꺼번에 DB에 저장할 예정)
                imageDTOList.add(
                    ImageDTO.builder()
                        .postId(postId)
                        .originalImgName(originName)
                        .savedImgName(savedName)
                        .imgPath(uploadPath)
                        .createDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build()
                );
            } catch (IOException e) {
                throw new ApiException(ErrorCode.FILE_IO_ERROR, "이미지 저장 도중에 발생한 파일 입출력 에러!!!");
            }
        }

        // 이미지 정보들 한꺼번에 DB에 저장
        imageMapper.insertImages(imageDTOList);
    }

    /**
     * postId로 이미지 정보들 가져오기
     */
    @Override
    public List<ImageDTO> getImages(Long postId) {
        return imageMapper.findImagesByPostId(postId);
    }

    @Override
    public void deleteByIds(List<Long> deleteImgIds) {
        int result = imageMapper.deleteByIds(deleteImgIds);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "이미지 삭제에 실패했습니다.");
        }
    }


}
