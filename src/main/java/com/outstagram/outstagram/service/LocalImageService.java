package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import com.outstagram.outstagram.util.Snowflake;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class LocalImageService extends AbstractBaseImageService {

    @Value("com.outstagram.upload.path")
    private String uploadPath;

    public LocalImageService(ImageMapper imageMapper, Snowflake snowflake0, Snowflake snowflake1) {
        super(imageMapper, snowflake0, snowflake1);
    }

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

    @Override
    public String uploadImage(MultipartFile image) {
        String savedName = UUID.randomUUID().toString().substring(0, 10) + image.getOriginalFilename();
        Path savePath = Paths.get(uploadPath, savedName);

        try {
            Files.copy(image.getInputStream(), savePath);

        } catch (IOException e) {
            throw new ApiException(e, ErrorCode.FILE_IO_ERROR);
        }

        return uploadPath + savedName;
    }

    @Override
    public void deleteRealImages(List<ImageDTO> deletedImages) {
        if (deletedImages.isEmpty()) return;
        for (ImageDTO image : deletedImages) {
            String imageUrl = image.getImgUrl();
            File file = new File(imageUrl);

            if (file.exists()) {
                if (file.delete()) {
                    log.info("============Deleted file : " + file.getAbsolutePath());
                } else {
                    log.error("============Failed to delete file : " + file.getAbsolutePath());
                }
            } else {
                log.error("============File not found : " + file.getAbsolutePath());
            }
        }
    }
}
