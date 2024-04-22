package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class EditPostRes {

    private String contents;

    private List<MultipartFile> imgFiles;

    private String authorName;
}
