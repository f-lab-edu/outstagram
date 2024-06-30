package com.outstagram.outstagram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.service.UserElasticsearchService;
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
@WebMvcTest(UserElasticController.class)
public class UserElasticControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserElasticsearchService userElasticsearchService;

    private MockHttpSession session;

    private UserDocument userDocument1;

    private UserDocument userDocument2;

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

        userDocument1 = UserDocument.builder()
                .id(1L)
                .nickname("testNickname")
                .email("test12345@test.com")
                .build();

        userDocument2 = UserDocument.builder()
                .id(2L)
                .nickname("testName!")
                .email("test990501@test.com")
                .build();

        session.setAttribute(LOGIN_USER, user);
    }

    @Test
    public void testSearchNickname() throws Exception {
        String searchTerm = "test";

        when(userElasticsearchService.findByNickname(searchTerm)).thenReturn(Arrays.asList(userDocument1, userDocument2));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/elasticsearch-users/nicknames")
                        .param("search", searchTerm)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].userId").value(userDocument1.getId()))
                .andExpect(jsonPath("$[0].nickname").value(userDocument1.getNickname()))
                .andExpect(jsonPath("$[1].userId").value(userDocument2.getId()))
                .andExpect(jsonPath("$[1].nickname").value(userDocument2.getNickname()));
    }

    @Test
    public void testGetUser() throws Exception {
        long userId = 1L;

        when(userElasticsearchService.findById(userId)).thenReturn(userDocument1);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/elasticsearch-users/{userId}", userId)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.userId").value(userDocument1.getId()))
                .andExpect(jsonPath("$.nickname").value(userDocument1.getNickname()))
                .andExpect(jsonPath("$.email").value(userDocument1.getEmail()))
                .andExpect(jsonPath("$.imgUrl").value(userDocument1.getImgUrl()));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        when(userElasticsearchService.findAll()).thenReturn(Arrays.asList(userDocument1, userDocument2));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/elasticsearch-users/all")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").value(userDocument1.getId()))
                .andExpect(jsonPath("$[0].nickname").value(userDocument1.getNickname()))
                .andExpect(jsonPath("$[0].email").value(userDocument1.getEmail()))
                .andExpect(jsonPath("$[1].id").value(userDocument2.getId()))
                .andExpect(jsonPath("$[1].nickname").value(userDocument2.getNickname()))
                .andExpect(jsonPath("$[1].email").value(userDocument2.getEmail()));
    }
}
