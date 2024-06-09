package com.outstagram.outstagram.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookmarkDTO implements Serializable {

    private Long userId;

    private Long postId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    public BookmarkDTO(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
