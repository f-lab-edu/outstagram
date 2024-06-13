package com.outstagram.outstagram.dto;

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
public class BookmarkRecordDTO implements Serializable {
    private Long postId;
    private LocalDateTime bookmarkAt;

}
