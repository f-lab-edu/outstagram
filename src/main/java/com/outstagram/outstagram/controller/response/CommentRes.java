package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.dto.ReplyUserDTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRes {
    private Long commentId;

    private Long userId;

    private String userImgUrl;

    private String nickname;

    private String contents;

    private LocalDateTime updateDate;

    private List<ReplyUserDTO> replyList;

}
