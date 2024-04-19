package com.outstagram.outstagram.service;

import com.outstagram.outstagram.mapper.ImageMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageMapper imageMapper;


    public void insertImages(List<MultipartFile> imgFiles) {

    }
}
