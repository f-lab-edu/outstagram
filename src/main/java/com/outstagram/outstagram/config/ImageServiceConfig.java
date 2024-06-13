package com.outstagram.outstagram.config;

import com.outstagram.outstagram.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * application.properties의 설정에 따라
 * Spring이 LocalImageService 또는 S3ImageService를 주입
 */
@Configuration
public class ImageServiceConfig {

    @Value("${image.service.type}")
    private String imageServiceType;

    @Autowired
    @Qualifier("localImageService")
    private ImageService localImageService;

    @Autowired
    @Qualifier("s3ImageService")
    private ImageService s3ImageService;

    @Bean
    public ImageService imageService() {
        if ("s3".equalsIgnoreCase(imageServiceType)) {
            return s3ImageService;
        } else {
            return localImageService;
        }
    }


}
