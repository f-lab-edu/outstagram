package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.request.PostCreateReq;
import com.outstagram.outstagram.controller.request.PostEditReq;
import com.outstagram.outstagram.controller.response.MyPostRes;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.PostMapper;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    private final ImageService imageService;
    private final UserService userService;

    public void insertPost(PostCreateReq postCreateReq, Long userId) {
        PostDTO newPost = PostDTO.builder()
            .contents(postCreateReq.getContents())
            .userId(userId)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        // 게시물 내용 저장 (insertPost 정상 실행되면, newPost의 id 속성에 id값이 들어 있다)
        postMapper.insertPost(newPost);

        // 로컬 디렉토리에 이미지 저장 후, DB에 이미지 정보 저장
        imageService.saveImages(postCreateReq.getImgFiles(),
            newPost.getId());

    }

    public PostRes getPost(Long postId, Long userId) {
        // 1. Post 가져오기
        PostDTO post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 2. Post의 이미지 정보 가져오기
        List<ImageDTO> imageList = imageService.getImages(post.getId());

        // 3. PostRes를 위한 데이터 가져오기
        UserDTO author = userService.findByUserId(post.getUserId());
        boolean isAuthor = author.getId().equals(userId);   // 로그인한 유저가 작성한 Post인지 여부

        Map<Long, String> imageUrlMap = new HashMap<>();
        for (ImageDTO img : imageList) {
            imageUrlMap.put(img.getId(), img.getImgPath() + "\\" + img.getSavedImgName());
        }
        // 4. PostRes 만들어서 반환하기
        // TODO : isLiked, isBookmarked, comments 채우기
        return PostRes.builder()
            .authorName(author.getNickname())
            .authorImgUrl(author.getImgUrl())
            .contents(post.getContents())
            .postImgUrls(imageUrlMap)
            .likes(post.getLikes())
            .isLiked(null)
            .isBookmarked(null)
            .isAuthor(isAuthor)
            .comments(null)
            .build();
    }

    @Transactional
    public void editPost(Long postId, PostEditReq postEditReq, Long userId) {
        // 수정할 게시물 가져오기
        PostDTO post = postMapper.findById(postId);

        // 게시물 작성자인지 검증
        validatePostOwner(post, userId);

        // 삭제할 이미지가 있다면 삭제하기(soft delete)
        if (postEditReq.getDeleteImgIds() != null && !postEditReq.getDeleteImgIds().isEmpty()) {
            imageService.deleteByIds(postEditReq.getDeleteImgIds());
        }

        // 추가할 이미지가 있다면 추가하기
        if (postEditReq.getImgFiles() != null && !postEditReq.getImgFiles().isEmpty()) {
            imageService.saveImages(postEditReq.getImgFiles(),
                post.getId());
        }

        // 수정할 내용이 있다면 수정하기
        if (!Objects.equals(postEditReq.getContents(), post.getContents())) {
            int result = postMapper.updateContentsById(postId, postEditReq.getContents());
            if (result == 0) {
                throw new ApiException(ErrorCode.UPDATE_ERROR, "게시물 내용 수정 오류!!");
            }
        }

    }

    public void deletePost(Long postId, Long userId) {
        // 삭제할 게시물 가져오기
        PostDTO post = postMapper.findById(postId);

        // 게시물 작성자인지 검증
        validatePostOwner(post, userId);

        // 게시물 삭제
        int result = postMapper.deleteById(postId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "게시물 삭제 오류 발생!!");
        }
    }

    /**
     * 게시물 작성자인지 검증 -> 수정, 삭제 시 확인 필요
     */
    private void validatePostOwner(PostDTO post, Long userId) {
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND, "해당 게시물은 존재하지 않습니다.");
        }

        // 게시물 작성자인지 확인
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "게시물에 대한 권한이 없습니다.");
        }
    }


    // TODO : like, bookmark 개발 후, 해당 내용 채우기
    public List<MyPostRes> getMyPosts(Long userId) {
        // 유저가 작성한 게시물 목록 최신 순으로 가져오기
        List<PostDTO> postList = postMapper.findByUserId(userId);

        return postList.stream()
            .map(post -> {
                // 각 게시물의 첫번째 이미지 가져오기
                ImageDTO firstImage = imageService.getFirstImage(post.getId());
                Map<Long, String> imageUrlMap = new HashMap<>();
                imageUrlMap.put(firstImage.getId(),
                    firstImage.getImgPath() + "\\" + firstImage.getSavedImgName());

                return MyPostRes.builder()
                    .contents(post.getContents())
                    .likes(post.getLikes())
                    .isLiked(null)
                    .isBookmarked(null)
                    .postImgUrl(imageUrlMap)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
