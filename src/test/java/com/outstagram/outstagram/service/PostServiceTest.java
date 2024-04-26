package com.outstagram.outstagram.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Mock
    private ImageService imageService;

    @Mock
    private UserService userService;

    @Mock
    private LikeService likeService;

    @Test
    public void testInsertPost_Success() {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);
        CreatePostReq createPostReq = CreatePostReq.builder()
                .contents("게시물 내용입니다.")
                .imgFiles(List.of(mockFile))
                .build();
        Long userId = 1L;

        // when
        // postMapper의 insertPost 메서드가 호출될 때, postId를 1L로 세팅
        when(postMapper.insertPost(any(PostDTO.class))).thenAnswer(invocation -> {
            PostDTO p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        });
        postService.insertPost(createPostReq, userId);

        // then
        // PostDTO 타입의 어떤 객체든지 상관 없이 insertPost가 정확히 1번 호출되었는지 검증해주는 코드
        verify(postMapper).insertPost(any(PostDTO.class));
        verify(imageService).saveImages(anyList(), eq(1L));
    }

    @Test
    public void testGetPost_Success() {
        Long userId = 1L;
        Long postId = 1L;
        PostDTO post = PostDTO.builder()
                .id(postId)
                .userId(userId)
                .contents("test post contents")
                .likes(100)
                .build();

        UserDTO user = UserDTO.builder()
                .id(userId)
                .nickname("testNickname")
                .imgUrl("www.userImageUrl.com")
                .build();

        List<ImageDTO> imageList = Arrays.asList(ImageDTO.builder().postId(postId).build());

        when(postMapper.findById(postId)).thenReturn(post);
        when(imageService.getImages(postId)).thenReturn(imageList);
        when(userService.findByUserId(userId)).thenReturn(user);

        PostRes foundPost = postService.getPost(postId, userId);

        assertNotNull(foundPost);
        assertTrue(foundPost.getIsAuthor());
        assertEquals("test post contents", foundPost.getContents());
        assertEquals(imageList.size(), foundPost.getPostImgUrls().size());
    }

    @Test
    public void testGetPost_Fail_PostNotFound() {
        // given
        Long userId = 1L;
        Long postId = 100L; // 없는 post id

        when(postMapper.findById(postId)).thenReturn(null);

        ApiException apiException = assertThrows(ApiException.class,
            () -> postService.getPost(postId, userId));

        assertEquals(ErrorCode.POST_NOT_FOUND, apiException.getErrorCode());
        assertEquals("해당 게시물은 존재하지 않습니다.", apiException.getDescription());

    }

    @Test
    public void testGetMyPosts() {
        Long userId = 1L;
        List<PostImageDTO> mockPostImages = Arrays.asList(
            PostImageDTO.builder()
                .id(1L)
                .contents("content01")
                .userId(userId)
                .imgPath("path01")
                .savedImgName("savedImgName01")
                .build(),
            PostImageDTO.builder()
                .id(2L)
                .contents("content02")
                .userId(userId)
                .imgPath("path02")
                .savedImgName("savedImgName02")
                .build()
        );

        when(postMapper.findWithImageByUserId(userId)).thenReturn(mockPostImages);
        when(likeService.existsLike(userId, 1L)).thenReturn(true);
        when(likeService.existsLike(userId, 2L)).thenReturn(false);

        List<MyPostsRes> myPosts = postService.getMyPosts(userId);

        assertNotNull(myPosts);
        assertEquals(mockPostImages.size(), myPosts.size());
        assertEquals(
            mockPostImages.get(0).getImgPath() + "\\" + mockPostImages.get(0).getSavedImgName(),
            myPosts.get(0).getThumbnailUrl()
        );
        assertEquals(
            mockPostImages.get(1).getImgPath() + "\\" + mockPostImages.get(1).getSavedImgName(),
            myPosts.get(1).getThumbnailUrl()
        );
        assertTrue(myPosts.get(0).getIsLiked());
        assertFalse(myPosts.get(1).getIsLiked());

        verify(postMapper).findWithImageByUserId(userId);
        verify(likeService).existsLike(userId, 1L);
        verify(likeService).existsLike(userId, 2L);
    }

    @Test
    public void testEditPost() {
        Long userId = 1L;
        Long postId = 1L;
        PostDTO mockPost = PostDTO.builder().id(postId).userId(userId).contents("before contents")
            .build();

        EditPostReq postEditReq = EditPostReq.builder().contents("after contents")
            .deleteImgIds(new ArrayList<>()).build();

        when(postMapper.findById(postId)).thenReturn(mockPost);
        when(postMapper.updateContentsById(postId, "after contents")).thenReturn(1);

        assertDoesNotThrow(() -> postService.editPost(postId, postEditReq, userId));
        verify(postMapper).updateContentsById(postId, "after contents");
    }

    @Test
    public void testDeletePost() {
        Long userId = 1L;
        Long postId = 1L;
        PostDTO mockPost = PostDTO.builder().id(postId).userId(userId).contents("contents").build();

        when(postMapper.findById(postId)).thenReturn(mockPost);
        when(postMapper.deleteById(postId)).thenReturn(1);

        assertDoesNotThrow(() -> postService.deletePost(postId, userId));
        verify(postMapper).deleteById(postId);
    }

    @Test
    public void testIncreaseLike() {
        Long postId = 1L;
        Long userId = 1L;

        when(postMapper.updateLikeCount(postId, 1)).thenReturn(1);

        assertDoesNotThrow(() -> postService.increaseLike(postId, userId));
        verify(postMapper).updateLikeCount(postId, 1);
        verify(likeService).insertLike(userId, postId);
    }

    @Test
    public void testUnlikePost() {
        Long postId = 1L;
        Long userId = 1L;

        when(postMapper.updateLikeCount(postId, -1)).thenReturn(1);

        assertDoesNotThrow(() -> postService.unlikePost(postId, userId));
        verify(postMapper).updateLikeCount(postId, -1);
        verify(likeService).deleteLike(userId, postId);

    }

    @Test
    public void testGetLikePosts() {
        Long userId = 1L;
        List<Long> mockLikePostIds = Arrays.asList(2L, 3L, 4L);
        List<PostImageDTO> mockPostImages = Arrays.asList(
            PostImageDTO.builder()
                .id(2L)
                .contents("content02")
                .likes(100)
                .userId(userId)
                .imgPath("path02")
                .savedImgName("savedImgName02")
                .build(),
            PostImageDTO.builder()
                .id(3L)
                .contents("content03")
                .likes(9999)
                .userId(userId)
                .imgPath("path03")
                .savedImgName("savedImgName03")
                .build(),
            PostImageDTO.builder()
                .id(4L)
                .contents("content04")
                .likes(36)
                .userId(userId)
                .imgPath("path04")
                .savedImgName("savedImgName04")
                .build()
        );

        when(likeService.getLikePosts(userId)).thenReturn(mockLikePostIds);
        when(postMapper.findLikePostsWithImageByPostIds(mockLikePostIds)).thenReturn(mockPostImages);

        List<MyPostsRes> likePosts = postService.getLikePosts(userId);

        assertNotNull(likePosts);
        assertEquals(
            mockPostImages.get(0).getImgPath() + "\\" + mockPostImages.get(0).getSavedImgName(),
            likePosts.get(0).getThumbnailUrl()
        );
        assertTrue(likePosts.get(0).getIsLiked());
        assertEquals(mockPostImages.get(1).getLikes(), likePosts.get(1).getLikes());

        verify(likeService).getLikePosts(userId);
        verify(postMapper).findLikePostsWithImageByPostIds(mockLikePostIds);
    }

}