package com.outstagram.outstagram.controller.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyRes {

    private Long replyId; // comment 테이블의 id

    private String userImgUrl;

    private String nickname;

    private String contents;

    private LocalDateTime updateDate;


}
