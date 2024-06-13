package com.outstagram.outstagram.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentUserDTO implements Serializable {
    private Long id;

    private Long userId;

    private Long postId;

    private Long parentCommentId;

    private String contents;

    private Boolean level;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private List<ReplyUserDTO> replyList;

    private String userImgUrl;

    private String nickname;

}
