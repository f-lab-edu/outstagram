package com.outstagram.outstagram.kafka.consumer;

import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.service.PostElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.outstagram.outstagram.common.constant.KafkaConst.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostConsumer {

    private final PostElasticsearchService postElasticsearchService;
    @KafkaListener(topics = POST_UPSERT_TOPIC, groupId = POST_GROUPID, containerFactory = "postKafkaListenerContainerFactory")
    public void upsert(@Payload PostDTO post) {
        log.info("========== START UPSERTING POST ==========");
        PostDocument document = convertPostDTOtoPostDocument(post);
        postElasticsearchService.save(document);
        log.info("========== END UPSERTING POST ==========");
    }

    /**
     * PostDTO -> PostDocument로 변환
     */
    private static PostDocument convertPostDTOtoPostDocument(PostDTO post) {
        return PostDocument.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .contents(post.getContents())
                .createDate(post.getCreateDate())
                .updateDate(post.getUpdateDate())
                .build();
    }
}
