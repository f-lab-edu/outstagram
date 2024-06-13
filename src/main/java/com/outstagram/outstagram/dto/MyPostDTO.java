package com.outstagram.outstagram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPostDTO {

    private Long postId;

    private String contents;

    private String postThumbnailImage;

    private Integer likes;

    private Integer commentCount;

    private Boolean isLiked;         // 현재 로그인한 유저가 해당 게시물을 좋아요 눌렀는지 여부

    private Boolean isBookmarked;    // 현재 로그인한 유저가 해당 게시물을 북마크 했는지 여부

}
