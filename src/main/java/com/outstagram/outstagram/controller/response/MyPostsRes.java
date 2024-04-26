package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MyPostsRes {


    private String contents;

    private String thumbnailUrl;

    private Integer likes;

    private Boolean isLiked;         // 현재 로그인한 유저가 해당 게시물을 좋아요 눌렀는지 여부

    private Boolean isBookmarked;    // 현재 로그인한 유저가 해당 게시물을 북마크 했는지 여부

}
