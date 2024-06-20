package com.outstagram.outstagram.kafka.consumer;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.service.UserElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.outstagram.outstagram.common.constant.KafkaConst.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {

    private final UserElasticsearchService userElasticsearchService;

    @KafkaListener(topics = USER_SAVE_TOPIC, groupId = USER_GROUPID, containerFactory = "userKafkaListenerContainerFactory")
    public void save(@Payload UserDTO user) {
        log.info("========== START SAVING USER ==========");
        UserDocument document = UserDocument.builder()
                .id(user.getId())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
        userElasticsearchService.save(document);
        log.info("========== END SAVING USER ==========");
    }

}
