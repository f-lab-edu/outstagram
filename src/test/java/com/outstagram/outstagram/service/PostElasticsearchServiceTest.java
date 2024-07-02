package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.repository.PostElasticsearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostElasticsearchServiceTest {

    @Mock
    private PostElasticsearchRepository postElasticsearchRepository;

    @InjectMocks
    private PostElasticsearchService postElasticsearchService;

    private PostDocument post1;
    private PostDocument post2;

    @BeforeEach
    public void setPostDocuments() {
        post1 = PostDocument.builder()
                .id(1L)
                .userId(1L)
                .contents("test PostDocument Contents 01")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        post2 = PostDocument.builder()
                .id(2L)
                .userId(2L)
                .contents("test PostDocument Contents 02")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    @Test
    public void testSavePost_Success() {
        postElasticsearchService.save(post1);

        verify(postElasticsearchRepository, times(1)).save(post1);
    }

    @Test
    public void testSavePost_InsertError() {
        given(postElasticsearchRepository.save(post1)).willThrow(new ApiException(ErrorCode.INSERT_ERROR));

        ApiException exception = assertThrows(ApiException.class, () -> postElasticsearchService.save(post1));

        assertEquals("DB insert 에러!!", exception.getDescription());
    }

    @Test
    public void testFindById_Success() {
        long postId = 1L;
        when(postElasticsearchRepository.findById(postId)).thenReturn(Optional.ofNullable(post1));

        PostDocument findPost = postElasticsearchService.findById(postId);

        assertEquals(post1.getId(), findPost.getId());
        assertEquals(post1.getContents(), findPost.getContents());
    }

    @Test
    public void testFindById_Fail_PostNotFound() {
        long postId = 3L;
        when(postElasticsearchRepository.findById(postId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> postElasticsearchService.findById(postId));

        assertEquals(ErrorCode.POST_NOT_FOUND_ESDB, exception.getErrorCode());
    }

    @Test
    public void testSearchByNickname_Success() {
        String searchText = "test";
        when(postElasticsearchService.findByKeyword(searchText)).thenReturn(Arrays.asList(post1, post2));

        List<PostDocument> findPostList = postElasticsearchService.findByKeyword(searchText);

        assertNotNull(findPostList);
        assertEquals(post1.getId(), findPostList.get(0).getId());
        assertEquals(post1.getContents(), findPostList.get(0).getContents());
        assertEquals(post2.getId(), findPostList.get(1).getId());
        assertEquals(post2.getContents(), findPostList.get(1).getContents());
    }

    @Test
    public void testFindAll() {
        when(postElasticsearchRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        Iterable<PostDocument> allPosts = postElasticsearchService.findAll();

        assertNotNull(allPosts);
        List<PostDocument> resultList = (List<PostDocument>) allPosts;
        assertEquals(2, resultList.size());
        assertEquals(post1, resultList.get(0));
        assertEquals(post2, resultList.get(1));
    }

    @Test
    public void testDeleteById() {
        long postId = 1L;

        postElasticsearchService.deleteById(postId);

        verify(postElasticsearchRepository, times(1)).deleteById(postId);
    }
}