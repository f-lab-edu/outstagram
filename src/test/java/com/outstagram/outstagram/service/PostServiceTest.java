package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.request.PostCreateReq;
import com.outstagram.outstagram.controller.response.PostRes;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void testInsertPost_Success() {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);
        PostCreateReq postCreateReq = PostCreateReq.builder()
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
        postService.insertPost(postCreateReq, userId);

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
        when(imageService.getImageInfo(postId)).thenReturn(imageList);
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

        ApiException apiException = assertThrows(ApiException.class, () -> postService.getPost(postId, userId));

        assertEquals(ErrorCode.POST_NOT_FOUND, apiException.getErrorCode());
        assertEquals("해당 게시물은 존재하지 않습니다.", apiException.getDescription());

    }

}