package com.outstagram.outstagram.controller.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRes implements Serializable {
    private Long postId;

    private Long userId;

    private String nickname;      // 게시물 작성자의 이름

    private String userImgUrl;  // 게시물 작성자의 프로필 사진(nullable)

    private String contents;

    private Map<Long, String> postImgUrls;

    private Integer likes;

    private Boolean likedByCurrentUser;         // 현재 로그인한 유저가 해당 게시물을 좋아요 눌렀는지 여부

    private Boolean bookmarkedByCurrentUser;    // 현재 로그인한 유저가 해당 게시물을 북마크 했는지 여부

    private Boolean isCreatedByCurrentUser;        // 현재 로그인한 유저가 해당 게시물 작성했는지 여부

    private List<CommentRes> comments;   // 게시물의 댓글 목록


}
