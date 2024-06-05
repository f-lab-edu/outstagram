package com.outstagram.outstagram.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyUserDTO implements Serializable {

    private Long replyId; // comment 테이블의 id

    private Long userId;

    private Long parentCommentId;

    private String contents;

    private Boolean level;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private String userImgUrl;

    private String nickname;
}
