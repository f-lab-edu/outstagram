package com.outstagram.outstagram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.dto.*;
import com.outstagram.outstagram.service.NotificationService;
import com.outstagram.outstagram.service.PostService;
import com.outstagram.outstagram.service.UserService;
import com.outstagram.outstagram.util.SHA256Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;


    private MockHttpSession session;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
        UserDTO user = UserDTO.builder()
                .id(1L)
                .nickname("test")
                .email("test@test.com")
                .password(SHA256Util.encryptedPassword("testPassword"))
                .imgUrl("www.testImgUrl.com")
                .isDeleted(false).createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        session.setAttribute(LOGIN_USER, user);
    }

    @Test
    public void testGetNotifications() throws Exception {
        List<NotificationDetailsDTO> notifications = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            NotificationDetailsDTO notification = new NotificationDetailsDTO();
            notification.setId((long) i);
            notifications.add(notification);
        }

        when(notificationService.getNotificationDetailsPlusOne(anyLong(), anyLong())).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationList", hasSize(6)))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    public void testReadNotification_Success_Follow() throws Exception {
        NotificationDTO notification = NotificationDTO.builder()
                .id(1L)
                .fromId(2L)
                .toId(1L)
                .targetId(1L)
                .alarmType(AlarmType.FOLLOW).
                isRead(false)
                .createDate(LocalDateTime.now().minusDays(1))
                .updateDate(LocalDateTime.now().minusDays(1))
                .build();

        UserDTO user = UserDTO.builder().id(2L).nickname("user2").email("user2@test.com").imgUrl("img2.jpg").build();

        when(notificationService.readNotification(anyLong(), anyLong())).thenReturn(notification);
        when(userService.getUser(anyLong())).thenReturn(user);

        mockMvc.perform(patch("/api/notifications/1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.nickname").value("user2"))
                .andExpect(jsonPath("$.email").value("user2@test.com"))
                .andExpect(jsonPath("$.imgUrl").value("img2.jpg"));
    }

    @Test
    public void testReadNotification_Success_Like() throws Exception {
        NotificationDTO notification = NotificationDTO.builder()
                .id(1L)
                .fromId(2L)
                .toId(1L)
                .targetId(1L)
                .alarmType(AlarmType.LIKE)
                .isRead(false)
                .createDate(LocalDateTime.now().minusDays(1))
                .updateDate(LocalDateTime.now().minusDays(1))
                .build();
        PostDetailsDTO postDetailsDTO = PostDetailsDTO.builder()
                .postId(1L)
                .build();

        when(notificationService.readNotification(anyLong(), anyLong())).thenReturn(notification);
        when(postService.getPostDetails(anyLong(), anyLong())).thenReturn(postDetailsDTO);

        mockMvc.perform(patch("/api/notifications/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testReadAllNotification_Success() throws Exception {
        doNothing().when(notificationService).readAllNotification(anyLong());

        mockMvc.perform(patch("/api/notifications/read-all").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("모든 알림을 읽음 처리 했습니다."))
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.isSuccess").value(true));
    }
}
