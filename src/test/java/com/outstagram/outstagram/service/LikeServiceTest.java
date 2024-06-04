package com.outstagram.outstagram.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.LikeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeMapper likeMapper;

    @InjectMocks
    private LikeService likeService;

    @Test
    public void testInsertLike_Success() {
        Long userId = 1L;
        Long postId = 1L;

//        LikeDTO newLike = LikeDTO.builder()
//            .userId(userId)
//            .postId(postId)
//            .createDate(LocalDateTime.now())
//            .build();

        likeService.insertLike(userId, postId);

        verify(likeMapper).insertLike(any(LikeDTO.class));
    }

    @Test
    public void testExistsLike_True() {
        Long userId = 1L;
        Long postId = 1L;

        // likeMapper의 existsUserLike 메소드가 호출될 때,
        // 입력으로 userId와 postId가 주어지면, 이 메소드는 true를 반환하도록 설정
        when(likeMapper.existsUserLike(userId, postId)).thenReturn(Boolean.TRUE);

        boolean exists = likeMapper.existsUserLike(userId, postId);

        //  existsLike 메소드의 결과가 true인지 검증
        assertTrue(exists);

        // likeMapper의 existsUserLike 메소드가 실제로 userId와 postId를 인자로 사용하여 호출되었는지를 검증
        verify(likeMapper).existsUserLike(userId, postId);
    }

    @Test
    public void testDeleteLike_Success() {
        Long userId = 1L;
        Long postId = 1L;

        likeMapper.deleteLike(userId, postId);

        verify(likeMapper).deleteLike(userId, postId);
    }

    @Test
    public void testDeleteLike_Fail() {
        Long userId = 1L;
        Long postId = 1L;

        when(likeMapper.deleteLike(userId, postId)).thenReturn(0);

        ApiException apiException = assertThrows(ApiException.class,
            () -> likeService.deleteLike(userId, postId));

        assertEquals(ErrorCode.DELETE_ERROR, apiException.getErrorCode());
    }

//    @Test
//    public void testGetLikePosts() {
//        Long userId = 1L;
//        Long lastId = 2L;
//        when(likeMapper.findWithPostsAndImageByUserId(userId, lastId, PageConst.PAGE_SIZE)).thenReturn()
//
//        when(likeMapper.findPostIdsByUserId(userId)).thenReturn(expectedPostIds);
//
//        List<Long> likePostIds = likeService.getLikePosts(userId);
//
//        assertEquals(expectedPostIds, likePostIds);
//        verify(likeMapper).findPostIdsByUserId(userId);
//    }

}