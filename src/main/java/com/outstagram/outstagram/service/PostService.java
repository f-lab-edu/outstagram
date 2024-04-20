package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    private final ImageService imageService;
    private final UserService userService;

    public void insertPost(CreatePostReq createPostReq, Long userId) {
        PostDTO newPost = PostDTO.builder()
            .contents(createPostReq.getContents())
            .userId(userId)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        // 게시물 내용 저장 (insertPost 정상 실행되면, newPost의 id 속성에 id값이 들어 있다)
        postMapper.insertPost(newPost);

        // 로컬 디렉토리에 이미지 저장 후, DB에 이미지 정보 저장
        imageService.saveImages(createPostReq.getImgFiles(),
            newPost.getId());

    }

    public PostRes getPost(Long postId, Long userId) {
        // 1. Post 가져오기
        PostDTO post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 2. Post의 이미지 정보 가져오기
        List<ImageDTO> imageList = imageService.getImageInfo(post.getId());

        // 3. PostRes를 위한 데이터 가져오기
        UserDTO author = userService.findByUserId(post.getUserId());
        boolean isAuthor = author.getId().equals(userId);   // 로그인한 유저가 작성한 Post인지 여부

        // 4. PostRes 만들어서 반환하기
        // TODO : isLiked, isBookmarked, comments 채우기
        return PostRes.builder()
            .authorName(author.getNickname())
            .authorImgUrl(author.getImgUrl())
            .contents(post.getContents())
            .postImgUrls(imageList.stream().map(entity ->
                entity.getImgPath() + "\\" + entity.getSavedImgName()).collect(Collectors.toList()))
            .likes(post.getLikes())
            .isLiked(null)
            .isBookmarked(null)
            .isAuthor(isAuthor)
            .comments(null)
            .build();
    }
}
