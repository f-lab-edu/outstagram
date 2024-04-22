package com.outstagram.outstagram.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostEditReq {
    @NotBlank(message = "게시물 내용은 필수 입력입니다.")
    private String contents;

    /**
     * imgFiles에는 새로 추가된 이미지만 넘어온다고 가정
     */
    @NotNull(message = "이미지는 최소 1장 이상 첨부해야 합니다.")
    private List<MultipartFile> imgFiles;

    private List<Long> deleteImgIds;
}
