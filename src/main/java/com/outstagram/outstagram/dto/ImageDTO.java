package com.outstagram.outstagram.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDTO {

    @Id
    private Long id;

    private Long postId;

    private String originalImgName;

    private String imgUrl;

    private Boolean isDeleted;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

}
