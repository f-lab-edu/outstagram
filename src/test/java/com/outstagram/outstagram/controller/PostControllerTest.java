package com.outstagram.outstagram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.controller.request.PostCreateReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import com.outstagram.outstagram.util.SHA256Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

import static com.outstagram.outstagram.common.session.SessionConst.LOGIN_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private MockHttpSession session;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
        UserDTO user = UserDTO.builder().id(1L).nickname("test").email("test@test.com")
                .password(SHA256Util.encryptedPassword("testPassword")).imgUrl("www.testImgUrl.com")
                .isDeleted(false).createDate(LocalDateTime.now()).updateDate(LocalDateTime.now())
                .build();
        session.setAttribute(LOGIN_USER, user);
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imgFiles", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts")
                        .file(mockFile)
                        .param("contents", "게시물 내용입니다.")
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    assertTrue(responseString.contains("게시물을 저장했습니다."));
                });
    }

    @Test
    public void testCreatePost_Fail_NoImage() throws Exception {
        PostCreateReq postCreateReq = PostCreateReq.builder()
                .contents("게시물 내용입니다.")
                .imgFiles(null)
                .build();

        String jsonContent = objectMapper.writeValueAsString(postCreateReq);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content(jsonContent)
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    // 응답에서 유효성 검사 관련 메시지를 확인
                    assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException());
                    MethodArgumentNotValidException ex = (MethodArgumentNotValidException) result.getResolvedException();
                    assertTrue(ex.getBindingResult().hasErrors());
                    assertEquals("이미지는 최소 1장 이상 첨부해야 합니다.", ex.getBindingResult().getFieldError("imgFiles").getDefaultMessage());
                });

    }

    @Test
    public void testCreatePost_Fail_NoContents() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("imgFiles", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts")
                        .file(mockFile)
                        .param("contents", "")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    // 응답에서 유효성 검사 관련 메시지를 확인
                    assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException());
                    MethodArgumentNotValidException ex = (MethodArgumentNotValidException) result.getResolvedException();
                    assertTrue(ex.getBindingResult().hasErrors());
                    assertEquals("게시물 내용은 필수 입력입니다.", ex.getBindingResult().getFieldError("contents").getDefaultMessage());
                });

    }
}