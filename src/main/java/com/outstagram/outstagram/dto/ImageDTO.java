package com.outstagram.outstagram.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDTO {

    @Id
    private Long id;

    private Long postId;

    @NotNull
    private String imgPath;

    private Boolean isDeleted;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

}
