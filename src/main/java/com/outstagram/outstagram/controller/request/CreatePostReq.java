package com.outstagram.outstagram.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePostReq {
    @NotBlank(message = "게시물 내용은 필수 입력입니다.")
    private String contents;

    @NotNull(message = "이미지는 최소 1장 이상 첨부해야 합니다.")
    private List<MultipartFile> imgFiles;
}
