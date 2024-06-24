package com.outstagram.outstagram.kafka.producer;

import com.outstagram.outstagram.dto.AlarmType;
import com.outstagram.outstagram.dto.NotificationDTO;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationDTO> notificationKafkaTemplate;

    public void send(String topic, Long fromId, Long targetId, AlarmType alarmType) {
        log.info("==== sending notification fromId({}) targetId({}) alarmType = {}", fromId, targetId, alarmType);

        NotificationDTO notification = NotificationDTO.builder()
            .alarmType(alarmType)
            .fromId(fromId)
            .targetId(targetId)
            .isRead(false)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        notificationKafkaTemplate.send(topic, notification);
    }
}
