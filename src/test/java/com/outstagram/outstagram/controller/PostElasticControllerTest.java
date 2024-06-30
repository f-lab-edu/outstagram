package com.outstagram.outstagram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostElasticsearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;

import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(PostElasticController.class)
public class PostElasticControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PostElasticsearchService postElasticsearchService;

    private MockHttpSession session;

    private PostDocument post1;

    private PostDocument post2;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
        UserDTO user = UserDTO.builder()
                .nickname("testNickname")
                .email("test@test.com")
                .password("testPassword")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .isDeleted(false)
                .build();

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

        session.setAttribute(LOGIN_USER, user);
    }

    @Test
    public void testSearchPosts() throws Exception {
        String searchText = "test";

        when(postElasticsearchService.findByKeyword(searchText)).thenReturn(Arrays.asList(post1, post2));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/elasticsearch-posts")
                        .param("searchText", searchText)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").value(post1.getId()))
                .andExpect(jsonPath("$[0].contents").value(post1.getContents()))
                .andExpect(jsonPath("$[1].id").value(post2.getId()))
                .andExpect(jsonPath("$[1].contents").value(post2.getContents()));
    }

    @Test
    public void testGetAllPosts() throws Exception {
        when(postElasticsearchService.findAll()).thenReturn(Arrays.asList(post1, post2));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/elasticsearch-posts/all")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").value(post1.getId()))
                .andExpect(jsonPath("$[0].userId").value(post1.getUserId()))
                .andExpect(jsonPath("$[0].contents").value(post1.getContents()))
                .andExpect(jsonPath("$[1].id").value(post2.getId()))
                .andExpect(jsonPath("$[1].userId").value(post2.getUserId()))
                .andExpect(jsonPath("$[1].contents").value(post2.getContents()));
    }
}
