package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;
import static com.outstagram.outstagram.dto.AlarmType.LIKE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Test
    public void testInsertNotification_Success() {
        // given
        NotificationDTO notification = NotificationDTO.builder()
                .fromId(1L)
                .toId(2L)
                .targetId(1L)
                .alarmType(LIKE)
                .isRead(false)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // when
        notificationService.insertNotification(notification);
        when(notificationMapper.findByIdAndUserId(1L, 2L)).thenReturn(notification);
        NotificationDTO findNotification = notificationMapper.findByIdAndUserId(1L, 2L);

        // then
        assertEquals(findNotification.getFromId(), notification.getFromId());
        assertEquals(findNotification.getToId(), notification.getToId());
    }

    @Test
    public void testGetNotificationDetailsPlusOne_Success() {
        // given
        NotificationDTO notification1 = NotificationDTO.builder()
                .id(1L)
                .fromId(2L)
                .toId(1L)
                .targetId(1L)
                .alarmType(LIKE)
                .isRead(false)
                .createDate(LocalDateTime.now().minusDays(1))
                .updateDate(LocalDateTime.now().minusDays(1))
                .build();
        NotificationDTO notification2 = NotificationDTO.builder()
                .id(2L)
                .fromId(2L)
                .toId(1L)
                .targetId(2L)
                .alarmType(LIKE)
                .isRead(false)
                .createDate(LocalDateTime.now().minusDays(2))
                .updateDate(LocalDateTime.now().minusDays(2))
                .build();
        when(notificationMapper.findByUserIdAndLastId(1L, 1L, PAGE_SIZE + 1)).thenReturn(Arrays.asList(notification1, notification2));

        UserDTO mockUser = UserDTO.builder()
                .id(1L)
                .nickname("user1")
                .imgUrl("img1.jpg")
                .build();
        ImageDTO mockImage = ImageDTO.builder()
                .id(1L)
                .imgUrl("img1.jpg")
                .build();
        when(userService.getUser(anyLong())).thenReturn(mockUser);
        when(imageService.getImageInfos(anyLong())).thenReturn(Collections.singletonList(mockImage));

        // when
        List<NotificationDetailsDTO> notifications = notificationService.getNotificationDetailsPlusOne(1L, 1L);

        // then
        assertEquals(2, notifications.size());
        assertEquals("user1", notifications.get(0).getFromNickname());
        assertEquals("img1.jpg", notifications.get(0).getPostImgUrl());
    }

    @Test
    public void testReadNotification_Success() {
        // given
        NotificationDTO mockNotification = NotificationDTO.builder()
                .fromId(1L)
                .toId(2L)
                .targetId(1L)
                .alarmType(LIKE)
                .isRead(false)
                .createDate(LocalDateTime.now().minusDays(1))
                .updateDate(LocalDateTime.now().minusDays(1))
                .build();
        when(notificationMapper.findByIdAndUserId(1L, 2L)).thenReturn(mockNotification);

        // when
        NotificationDTO notification = notificationService.readNotification(1L, 2L);

        // then
        assertTrue(notification.getIsRead());
        verify(notificationMapper, times(1)).readNotification(1L, 2L);
    }

    @Test
    public void testReadNotification_Failure_NotFound() {
        // given
        when(notificationMapper.findByIdAndUserId(3L, 2L)).thenReturn(null);

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> notificationService.readNotification(3L, 2L));
        assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, exception.getErrorCode());
    }

    @Test
    public void testDeleteOlderThan30Days_Success() {
        // when
        notificationService.deleteOlderThan30Days();

        // then
        verify(notificationMapper, times(1)).deleteOlderThan30Days();
    }

    @Test
    public void testDeleteOlderThan30Days_Failure_NoOldNotifications() {
        // given
        doNothing().when(notificationMapper).deleteOlderThan30Days();

        // when
        notificationService.deleteOlderThan30Days();

        // then
        verify(notificationMapper, times(1)).deleteOlderThan30Days();
    }
}