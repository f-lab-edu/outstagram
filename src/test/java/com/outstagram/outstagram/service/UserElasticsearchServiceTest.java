package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.repository.UserElasticsearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserElasticsearchServiceTest {

    @Mock
    private UserElasticsearchRepository userElasticsearchRepository;

    @InjectMocks
    private UserElasticsearchService userElasticsearchService;

    private UserDocument user;

    @BeforeEach
    public void setUserDocument() {
        user = UserDocument.builder()
                .id(1L)
                .nickname("testNickname")
                .email("test12345@test.com")
                .build();
    }

    @Test
    public void testSaveUser_Success() {
        // given

    }

}