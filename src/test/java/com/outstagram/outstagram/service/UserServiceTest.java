package com.outstagram.outstagram.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.kafka.producer.UserProducer;
import com.outstagram.outstagram.mapper.UserMapper;
import com.outstagram.outstagram.util.SHA256Util;
import com.outstagram.outstagram.util.Snowflake;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private Snowflake snowflake;

    @Mock
    private UserProducer userProducer;

    @InjectMocks
    private UserService userService;

    private UserDTO user;

    @BeforeEach
    public void setUserInfo() {
        user = UserDTO.builder().nickname("testNickname").email("test@test.com")
            .password("testPassword").createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now()).isDeleted(false).build();
    }


    // 유저 저장 테스트
    @Test
    public void testInsertUser_Success() {
        // given
        given(userMapper.countByEmail(user.getEmail())).willReturn(0);
        given(userMapper.countByNickname(user.getNickname())).willReturn(0);
        if (user.getCreateDate().getSecond() % 2 == 0) {
            given(snowflake.nextId(0)).willReturn(123456L);
        } else {
            given(snowflake.nextId(1)).willReturn(123457L);
        }
        given(userMapper.insertUser(user)).willReturn(1);

        // when
        userService.insertUser(user);

        // then
        // UserService 클래스의 insertUser 메서드가 UserMapper의 insertUser 메서드를 올바른 매개변수(user DTO)와 함께 올바르게 호출했는지 확인
        verify(userMapper).insertUser(user);

    }

    @Test
    public void testInsertUser_Fail_EmailDuplicated() {
        // given
        given(userMapper.countByEmail(user.getEmail())).willReturn(1); // 이메일 중복

        // when
        ApiException exception = assertThrows(ApiException.class, () -> userService.insertUser(user));

        // then
        assertEquals("중복됩니다.", exception.getDescription());
    }

    @Test
    public void testInsertUser_Fail_NicknameDuplicated() {
        // given
        given(userMapper.countByNickname(user.getNickname())).willReturn(1); // 닉네임 중복

        // when
        ApiException exception = assertThrows(ApiException.class, () -> userService.insertUser(user));

        // then
        assertEquals("중복됩니다.", exception.getDescription());
    }

    // 로그인 테스트
    @Test
    public void testLogin_Success() {
        // given
        String encryptedPassword = SHA256Util.encryptedPassword(user.getPassword());
        given(userMapper.findByEmailAndPassword(user.getEmail(), encryptedPassword)).willReturn(user);

        // when
        UserDTO loginUser = userService.login(user.getEmail(), user.getPassword());

        // then
        assertNotNull(loginUser);
        assertEquals(user.getEmail(), loginUser.getEmail());
    }

}
