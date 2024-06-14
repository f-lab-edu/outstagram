package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.AlarmType;
import com.outstagram.outstagram.dto.NotificationDTO;
import java.time.LocalDateTime;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class NotificationMapperTest {

    @Autowired
    private NotificationMapper notificationMapper;

    NotificationDTO notification1;
    @Before("")
    public void setUp() {
        notification1 = NotificationDTO.builder()
            .alarmType(AlarmType.LIKE)
            .fromId(4L)
            .toId(6L)
            .targetId(97L)
            .isRead(false)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }
    @Test
    public void insertTest() {
        NotificationDTO notification = NotificationDTO.builder()
            .alarmType(AlarmType.LIKE)
            .fromId(4L)
            .toId(6L)
            .targetId(97L)
            .isRead(false)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        notificationMapper.insertNotification(notification);
    }

}