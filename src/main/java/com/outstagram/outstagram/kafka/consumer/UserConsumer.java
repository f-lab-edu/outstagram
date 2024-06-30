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

    @KafkaListener(topics = USER_UPSERT_TOPIC, groupId = USER_GROUPID, containerFactory = "userKafkaListenerContainerFactory")
    public void upsert(@Payload UserDTO user) {
        log.info("========== START UPSERTING USER ==========");
        UserDocument document = UserDocument.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
        userElasticsearchService.save(document);
        log.info("========== END UPSERTING USER ==========");
    }

    @KafkaListener(topics = USER_DELETE_TOPIC, groupId = USER_GROUPID, containerFactory = "userKafkaListenerContainerFactory")
    public void delete(@Payload UserDTO user) {
        log.info("========== START DELETING USER ==========");
        userElasticsearchService.deleteById(user.getId());
        log.info("========== END DELETING USER ==========");
    }

}
