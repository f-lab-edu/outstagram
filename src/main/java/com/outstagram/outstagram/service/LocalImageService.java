package com.outstagram.outstagram.service;

import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class LocalImageService extends AbstractImageService {

    @Value("com.outstagram.upload.path")
    private String uploadPath;

    public LocalImageService(ImageMapper imageMapper) {
        super(imageMapper);
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


}
