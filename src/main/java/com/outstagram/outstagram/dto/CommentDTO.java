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
public class CommentDTO {

    @Id
    private Long id;

    private Long userId;

    private Long postId;

    private Long parentCommentId;

    private String contents;

    private Boolean level;

    private Boolean isDeleted;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
