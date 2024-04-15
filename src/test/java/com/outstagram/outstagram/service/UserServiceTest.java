package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.mapper.UserMapper;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
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
    public void testInsertUser_유저_회원가입_성공() {
        //when
        int result = userMapper.insertUser(user);

        //then
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    public void testInsertUser_유저_회원가입_실패() {

    }

    // 로그인 테스트
    @Test
    public void testLogin_유저_로그인_성공() {

    }

}
