package com.outstagram.outstagram.controller.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreatePostReq {
    private String contents;
    private List<MultipartFile> imgFiles;

}
