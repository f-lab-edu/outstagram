package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.controller.response.MyPostsRes;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    private final ImageService imageService;
    private final UserService userService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    @Transactional
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

    // TODO : post 조회 쿼리, image 조회 쿼리, user 조회 쿼리, like 조회 쿼리 => 총 4개 쿼리 발생
    public List<MyPostsRes> getMyPosts(Long userId, Long lastId) {
        // 유저의 (게시물과 게시물의 대표이미지) 10개씩 가져오기
        List<PostImageDTO> postWithImgList = postMapper.findWithImageByUserId(userId, lastId, PAGE_SIZE);

        return postWithImgList.stream()
                .map(dto -> MyPostsRes.builder()
                        .postId(dto.getId())
                        .contents(dto.getContents())
                        .likes(dto.getLikes())
                        .thumbnailUrl(dto.getImgPath() + "\\" + dto.getSavedImgName())
                        .isLiked(likeService.existsLike(userId, dto.getId()))
                        .isBookmarked(bookmarkService.existsBookmark(userId, dto.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    public PostRes getPost(Long postId, Long userId) {
        // 1. Post 가져오기
        PostDTO post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 2. Post의 이미지 정보 가져오기
        List<ImageDTO> imageList = imageService.getImages(post.getId());

        // 3. 로그인한 유저가 게시물 작성자인지 판단
        boolean isAuthor = post.getUserId().equals(userId);

        // 4. 작성자 정보 가져오기 -> nickname과 유저 img 가져오기 위해서
        UserDTO author = userService.findByUserId(post.getUserId());

        // 5. 이미지 url 조합하기
        Map<Long, String> imageUrlMap = new HashMap<>();
        for (ImageDTO img : imageList) {
            imageUrlMap.put(img.getId(), img.getImgPath() + "\\" + img.getSavedImgName());
        }

        // TODO : comments 채우기
        return PostRes.builder()
                .authorName(author.getNickname())
                .authorImgUrl(author.getImgUrl())
                .contents(post.getContents())
                .postImgUrls(imageUrlMap)
                .likes(post.getLikes())
                .isLiked(likeService.existsLike(userId, post.getId()))
                .isBookmarked(bookmarkService.existsBookmark(userId, post.getId()))
                .isAuthor(isAuthor)
                .comments(null)
                .build();
    }

    @Transactional
    public void editPost(Long postId, EditPostReq editPostReq, Long userId) {
        // 수정할 게시물 가져오기
        PostDTO post = postMapper.findById(postId);

        // 게시물 작성자인지 검증
        validatePostOwner(post, userId);

        // 삭제할 이미지가 있다면 삭제하기(soft delete)
        if (editPostReq.getDeleteImgIds() != null && !editPostReq.getDeleteImgIds().isEmpty()) {
            imageService.deleteByIds(editPostReq.getDeleteImgIds());
        }

        // 추가할 이미지가 있다면 추가하기
        if (editPostReq.getImgFiles() != null && !editPostReq.getImgFiles().isEmpty()) {
            imageService.saveImages(editPostReq.getImgFiles(),
                    post.getId());
        }

        // 수정할 내용이 있다면 수정하기
        if (!Objects.equals(editPostReq.getContents(), post.getContents())) {
            int result = postMapper.updateContentsById(postId, editPostReq.getContents());
            if (result == 0) {
                throw new ApiException(ErrorCode.UPDATE_ERROR, "게시물 내용 수정 오류!!");
            }
        }

    }

    /**
     * 게시물 삭제 메서드 실제 레코드를 삭제하지 않고 is_deleted = 1 방식으로 soft_delete
     */
    @Transactional
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
     * 좋아요 증가 메서드 - 게시물의 좋아요 개수 증가 - like table에 row 추가하기
     */
    @Transactional
    public void increaseLike(Long postId, Long userId) {
        // 게시물 좋아요 1 증가
        int result = postMapper.updateLikeCount(postId, 1);
        if (result == 0) {
            throw new ApiException(ErrorCode.UPDATE_ERROR);
        }

        // like 테이블에 좋아요 기록 저장
        likeService.insertLike(userId, postId);
    }

    /**
     * 좋아요 취소 기능 - 게시물 좋아요 개수 1 감소 - like table에서 해당 기록 삭제
     */
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        // 게시물의 좋아요 개수 1 감소
        int result = postMapper.updateLikeCount(postId, -1);
        if (result == 0) {
            throw new ApiException(ErrorCode.UPDATE_ERROR);
        }

        // 좋아요 누른 기록 삭제
        likeService.deleteLike(userId, postId);
    }

    /**
     * 로그인한 유저가 좋아요 누른 모든 게시물 가져오기
     */
    public List<MyPostsRes> getLikePosts(Long userId, Long lastId) {
        // 유저가 좋아요 누른 게시물 Id 가져오기
        List<Long> likePosts = likeService.getLikePosts(userId);

        // 좋아요 누른 게시물 없으면 빈 list 반환
        if (likePosts.isEmpty()) {
            return new ArrayList<>();
        }

        // 좋아요 누른 게시물들 가져오기 (10개씩 가져오기)
        List<PostImageDTO> likedPostImageList = postMapper.findPostsWithImageByPostIds(likePosts, lastId, PAGE_SIZE);

        return likedPostImageList.stream()
            .map(dto -> MyPostsRes.builder()
                .postId(dto.getId())
                .contents(dto.getContents())
                .likes(dto.getLikes())
                .thumbnailUrl(dto.getImgPath() + "\\" + dto.getSavedImgName())
                .isLiked(true)  // 애초에 좋아요 누른 게시물의 정보를 가져온거임 그래서 무조건 true
                .isBookmarked(bookmarkService.existsBookmark(userId, dto.getId()))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 로그인한 유저가 북마크한 모든 게시물 가져오기
     */
    public List<MyPostsRes> getBookmarkedPosts(Long userId, Long lastId) {
        // 유저가 북마크한 게시물 Id 가져오기
        List<Long> bookmarkedPosts = bookmarkService.getBookmarkedPosts(userId);

        // 북마크한 게시물 없으면 빈 list 반환
        if (bookmarkedPosts.isEmpty()) {
            return new ArrayList<>();
        }

        // 북마크한 게시물들 가져오기
        List<PostImageDTO> bookmarkedPostImageList = postMapper.findPostsWithImageByPostIds(bookmarkedPosts, lastId, PAGE_SIZE);

        return bookmarkedPostImageList.stream()
            .map(dto -> MyPostsRes.builder()
                .postId(dto.getId())
                .contents(dto.getContents())
                .likes(dto.getLikes())
                .thumbnailUrl(dto.getImgPath() + "\\" + dto.getSavedImgName())
                .isLiked(likeService.existsLike(userId, dto.getId()))
                .isBookmarked(true) // 애초에 북마크한 게시물의 정보를 가져온거임 그래서 무조건 true
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 북마크 저장 메서드
     */
    @Transactional
    public void addBookmark(Long postId, Long userId) {
        bookmarkService.insertBookmark(userId, postId);
    }

    /**
     * 북마크 취소 메서드
     */
    @Transactional
    public void deleteBookmark(Long postId, Long userId) {
        bookmarkService.deleteBookmark(userId, postId);
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
}
