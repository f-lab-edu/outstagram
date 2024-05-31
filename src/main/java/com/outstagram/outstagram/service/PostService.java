package com.outstagram.outstagram.service;


import static com.outstagram.outstagram.common.constant.OptimisticLockConst.MAX_RETRIES;
import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.common.constant.CacheNames;
import com.outstagram.outstagram.controller.request.CreateCommentReq;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditCommentReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.controller.response.MyPostsRes;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.kafka.producer.FeedUpdateProducer;
import com.outstagram.outstagram.kafka.producer.PostDeleteProducer;
import com.outstagram.outstagram.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final CommentService commentService;

    private final FeedUpdateProducer feedUpdateProducer;
    private final PostDeleteProducer postDeleteProducer;

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

        Long newPostId = newPost.getId();

        // 로컬 디렉토리에 이미지 저장 후, DB에 이미지 정보 저장
        imageService.saveImages(createPostReq.getImgFiles(),
            newPostId);

        // kafka에 메시지 발행 : 팔로워들의 피드목록에 내가 작성한 게시물 ID 넣기
        feedUpdateProducer.send("feed", userId, newPostId);
    }

    // TODO : post 조회 쿼리, image 조회 쿼리, user 조회 쿼리, like 조회 쿼리 => 총 4개 쿼리 발생
    public List<MyPostsRes> getMyPosts(Long userId, Long lastId) {
        // 유저의 (게시물과 게시물의 대표이미지) 10개씩 가져오기
        List<PostImageDTO> postWithImgList = postMapper.findWithImageByUserId(userId, lastId,
            PAGE_SIZE);

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

    @Cacheable(value = CacheNames.POST, key = "#postId")
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

        return PostRes.builder()
            .authorName(author.getNickname())
            .authorImgUrl(author.getImgUrl())
            .contents(post.getContents())
            .postImgUrls(imageUrlMap)
            .likes(post.getLikes())
            .isLiked(likeService.existsLike(userId, post.getId()))
            .isBookmarked(bookmarkService.existsBookmark(userId, post.getId()))
            .isAuthor(isAuthor)
            .comments(commentService.getComments(post.getId()))
            .build();
    }

    @Transactional
    @Caching(evict = @CacheEvict(value = CacheNames.POST, key = "#postId"))
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
     * 게시물 삭제 비동기 처리
     */
    @Transactional
    @Caching(evict = @CacheEvict(value = CacheNames.POST, key = "#postId"))
    public void deletePost(Long postId, Long userId) {

        // 게시물이 존재하는지 & 삭제 권한 있는지 검증
        validatePostOwner(postId, userId);

        // 게시물 삭제 비동기 처리
        postDeleteProducer.send("post-delete", postId);
    }

    /* ========================================================================================== */

    /**
     * 좋아요 증가 메서드 - 게시물의 좋아요 개수 증가 - like table에 row 추가하기
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void increaseLike(Long postId, Long userId) {
        int attempt = 0;

        while (true) {
            try {
                // 버전 가져오기
                PostDTO post = postMapper.findById(postId);
                if (post == null) {
                    throw new ApiException(ErrorCode.POST_NOT_FOUND);
                }

                // 게시물 좋아요 1 증가
                int result = postMapper.updateLikeCount(postId, 1, post.getVersion());

                // 업데이트 성공 시
                if (result > 0) {
                    likeService.insertLike(userId, postId);
                    break;
                }

                // 최대 재시도 횟수 초과 시 예외 던짐
                if (attempt > MAX_RETRIES) {
                    throw new ApiException(ErrorCode.RETRY_EXCEEDED);
                }

                // 재시도 횟수 증가
                attempt++;
            } catch (Exception e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }


    }

    /**
     * 좋아요 취소 기능 - 게시물 좋아요 개수 1 감소 - like table에서 해당 기록 삭제
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void unlikePost(Long postId, Long userId) {
        int attempt = 0;

        while (true) {
            try {
                // 버전 가져오기
                PostDTO post = postMapper.findById(postId);
                if (post == null) {
                    throw new ApiException(ErrorCode.POST_NOT_FOUND);
                }

                // 게시물 좋아요 1 감소
                int result = postMapper.updateLikeCount(postId, -1, post.getVersion());

                // 업데이트 성공 시
                if (result > 0) {
                    likeService.deleteLike(userId, postId);
                    break;
                }

                // 최대 재시도 횟수 초과 시 예외 던짐
                if (attempt > MAX_RETRIES) {
                    throw new ApiException(ErrorCode.RETRY_EXCEEDED);
                }

                // 재시도 횟수 증가
                attempt++;
            } catch (Exception e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * 로그인한 유저가 좋아요 누른 모든 게시물 가져오기
     */
    public List<MyPostsRes> getLikePosts(Long userId, Long lastId) {
        List<PostImageDTO> likePosts = likeService.getLikePosts(userId, lastId);

        return likePosts.stream()
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

    /* ========================================================================================== */

    /**
     * 로그인한 유저가 북마크한 모든 게시물 가져오기
     */
    public List<MyPostsRes> getBookmarkedPosts(Long userId, Long lastId) {
        List<PostImageDTO> bookmarkPosts = bookmarkService.getBookmarkedPosts(userId, lastId);

        return bookmarkPosts.stream()
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

    /* ========================================================================================== */

    /**
     * 댓글 저장하는 로직
     */
    public void addComment(CreateCommentReq commentReq, Long postId, UserDTO user) {
        // 존재하는 post인지 검증
        PostDTO findPost = postMapper.findById(postId);
        if (findPost == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 댓글 객체 생성하기
        CommentDTO newComment = CommentDTO.builder()
            .userId(user.getId())
            .postId(postId)
            .parentCommentId(null)
            .contents(commentReq.getContents())
            .level(false)
            .isDeleted(false)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        // comment 테이블에 댓글 저장하기
        commentService.insertComment(newComment);
    }

    /**
     * 대댓글 저장하는 로직
     */
    public void addComment(CreateCommentReq commentReq, Long postId, Long commentId, UserDTO user) {
        // 존재하는 post인지 검증
        PostDTO findPost = postMapper.findById(postId);
        if (findPost == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 대댓글 객체 생성하기
        CommentDTO newComment = CommentDTO.builder()
            .userId(user.getId())
            .postId(postId)
            .parentCommentId(commentId)
            .contents(commentReq.getContents())
            .level(true)
            .isDeleted(false)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        // comment 테이블에 댓글 저장하기
        commentService.insertComment(newComment);
    }

    /**
     * (대)댓글 수정
     */
    public void editComment(EditCommentReq editCommentReq, Long postId, Long commentId,
        Long userId) {
        // 게시물&댓글 존재 여부 검증, 작성자인지 검증하기
        validatePostCommentAndOwnership(postId, commentId, userId);

        commentService.updateContents(commentId, editCommentReq.getContents());
    }

    public void deleteComment(Long postId, Long commentId, Long userId) {
        validatePostCommentAndOwnership(postId, commentId, userId);

        commentService.deleteComment(commentId);

    }



    /* ========================================================================================== */

    /**
     * 게시물 작성자인지 검증 -> 수정, 삭제 시 확인 필요
     */
    private void validatePostOwner(Long postId, Long userId) {
        PostDTO post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 게시물 작성자인지 확인
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "게시물에 대한 권한이 없습니다.");
        }
    }

    private void validatePostOwner(PostDTO post, Long userId) {
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 게시물 작성자인지 확인
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "게시물에 대한 권한이 없습니다.");
        }
    }

    /**
     * 게시물, 댓글 존재 여부 검증 댓글 작성자인지 검증
     */
    private void validatePostCommentAndOwnership(Long postId, Long commentId, Long userId) {
        // 게시물 존재 여부 검증
        PostDTO post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 댓글 존재 여부 검증
        CommentDTO comment = commentService.findById(commentId);
        if (comment == null) {
            throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 댓글 작성자인지 검증
        if (!userId.equals(comment.getUserId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "댓글에 대한 권한이 없습니다.");
        }

    }
}
