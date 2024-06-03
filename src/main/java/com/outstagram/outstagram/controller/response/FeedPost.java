package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedPost implements Serializable {

    // 게시물 id
    private Long postId;
    // 게시물 이미지
    private Map<Long, String> postImgUrls;
    // 게시물 내용
    private String contents;
    // 게시물 좋아요 개수
    private Integer likeCount;
    // 게시물 댓글 개수
    private Integer commentCount;
    // 게시물 본인 좋아요 여부
    private Boolean likedByCurrentUser;
    // 게시물 본인 북마크 여부
    private Boolean bookmarkedByCurrentUser;
    // 게시물 본인 작성 여부
    private Boolean isCreatedByCurrentUser;


    // 작성자 id
    private Long userId;
    // 작성자 닉네임
    private String nickname;
    // 작성자 프로필 이미지
    private String userImgUrl;

}
