package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.repository.UserElasticsearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserElasticsearchServiceTest {

    @Mock
    private UserElasticsearchRepository userElasticsearchRepository;

    @InjectMocks
    private UserElasticsearchService userElasticsearchService;

    private UserDocument user;

    private UserDocument user2;

    @BeforeEach
    public void setUserDocument() {
        user = UserDocument.builder()
                .id(1L)
                .nickname("testNickname")
                .email("test12345@test.com")
                .build();

        user2 = UserDocument.builder()
                .id(2L)
                .nickname("testName!")
                .email("test990501@test.com")
                .build();
    }

    @Test
    public void testSaveUser_Success() {
        userElasticsearchService.save(user);

        verify(userElasticsearchRepository, times(1)).save(user);
    }

    @Test
    public void testSaveUser_Error() {
        given(userElasticsearchRepository.save(user)).willThrow(new ApiException(ErrorCode.INSERT_ERROR));

        ApiException exception = assertThrows(ApiException.class, () -> userElasticsearchService.save(user));

        assertEquals("DB insert 에러!!", exception.getDescription());
    }

    @Test
    public void testSearchByNickname_Success() {
        String searchText = "test";
        when(userElasticsearchService.findByNickname(searchText)).thenReturn(Arrays.asList(user, user2));

        List<UserDocument> findUserList = userElasticsearchService.findByNickname(searchText);

        assertNotNull(findUserList);
        assertEquals(user.getNickname(), findUserList.get(0).getNickname());
        assertEquals(user2.getNickname(), findUserList.get(1).getNickname());
    }

    @Test
    public void testFindById_Success() {
        long userId = 1L;
        when(userElasticsearchRepository.findById(userId)).thenReturn(Optional.ofNullable(user));

        UserDocument findUser = userElasticsearchService.findById(userId);

        assertEquals(user.getId(), findUser.getId());
        assertEquals(user.getNickname(), findUser.getNickname());
    }

    @Test
    public void testFindById_Fail_UserNotFound() {
        long userId = 3L;
        when(userElasticsearchRepository.findById(userId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> userElasticsearchService.findById(userId));

        assertEquals(ErrorCode.USER_NOT_FOUND_ESDB, exception.getErrorCode());
    }

    @Test
    public void testFindAll() {
        when(userElasticsearchRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        Iterable<UserDocument> allUsers = userElasticsearchService.findAll();

        assertNotNull(allUsers);
        List<UserDocument> resultList = (List<UserDocument>) allUsers;
        assertEquals(2, resultList.size());
        assertEquals(user, resultList.get(0));
        assertEquals(user2, resultList.get(1));
    }

    @Test
    public void testDeleteById() {
        long userId = 1L;

        userElasticsearchService.deleteById(userId);

        verify(userElasticsearchRepository, times(1)).deleteById(userId);
    }
}