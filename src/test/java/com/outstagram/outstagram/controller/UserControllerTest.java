package com.outstagram.outstagram.controller;


import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.controller.request.UserLoginReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.UserService;
import com.outstagram.outstagram.util.SHA256Util;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private MockHttpSession session;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
    }

    @Test
    public void testIsDuplicatedEmail() throws Exception {
        String email = "test@test.com";
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/users/check-duplicated-email").param("email", email))
                .andExpect(status().isOk()).andExpect(content().string("해당 이메일 사용 가능합니다."));
    }

    @Test
    public void testIsDuplicatedNickname() throws Exception {
        String nickname = "test";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/check-duplicated-nickname")
                        .param("nickname", nickname)).andExpect(status().isOk())
                .andExpect(content().string("해당 닉네임이 사용 가능합니다."));
    }

    @Test
    public void testSignup() throws Exception {
        UserDTO user = createUserDTO();
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/signup").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))).andExpect(status().isOk())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        UserLoginReq loginReq = new UserLoginReq("test@test.com", "testPassword");
        UserDTO user = createUserDTO();

        // userService.login()이 호출되었을 때, 반환할 객체를 사전에 지정해놓음 (메서드 구현과 관계없이 항상 user 반환하도록 확정지을 수 있음)
        when(userService.login(anyString(), anyString())).thenReturn(user);

        session.setAttribute(LOGIN_USER, user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq))).andExpect(status().isOk())
                .andExpect(content().string("로그인 성공"));

        assert session.getAttribute(LOGIN_USER) != null;
    }

    @Test
    public void testLogin_Fail() throws Exception {
        UserLoginReq loginReq = new UserLoginReq("test@test.com", "testPassword");

        when(userService.login(anyString(), anyString())).thenThrow(
                new ApiException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isNotFound());

    }

    private UserDTO createUserDTO() {
        return UserDTO.builder().id(1L).nickname("test").email("test@test.com")
                .password(SHA256Util.encryptedPassword("testPassword")).imgUrl("www.testImgUrl.com")
                .isDeleted(false).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now())
                .build();
    }


}
