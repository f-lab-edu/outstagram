package com.outstagram.outstagram.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PostRes {

    private String authorName;      // 게시물 작성자의 이름

    private String authorImgUrl;  // 게시물 작성자의 프로필 사진(nullable)

    private String contents;

    private List<String> postImgUrls;

    private Integer likes;

    private Boolean isLiked;         // 현재 로그인한 유저가 해당 게시물을 좋아요 눌렀는지 여부

    private Boolean isBookmarked;    // 현재 로그인한 유저가 해당 게시물을 북마크 했는지 여부

    private Boolean isAuthor;        // 현재 로그인한 유저가 해당 게시물 작성했는지 여부

    private List<Object> comments;   // 게시물의 댓글 목록


}
