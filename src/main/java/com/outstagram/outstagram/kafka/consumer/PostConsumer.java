package com.outstagram.outstagram.kafka.consumer;

import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.service.PostElasticsearchService;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.outstagram.outstagram.common.constant.KafkaConst.*;
import static com.outstagram.outstagram.common.constant.KafkaConst.POST_EDIT_TOPIC;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostConsumer {

    private final PostElasticsearchService postElasticsearchService;
    @KafkaListener(topics = POST_SAVE_TOPIC, groupId = POST_GROUPID, containerFactory = "postKafkaListenerContainerFactory")
    public void save(@Payload PostDTO post) {
        log.info("========== START SAVING POST ==========");
        PostDocument document = convertPostDTOtoPostDocument(post);
        postElasticsearchService.save(document);
        log.info("========== END SAVING POST ==========");
    }

    @KafkaListener(topics = POST_EDIT_TOPIC, groupId = POST_GROUPID, containerFactory = "postKafkaListenerContainerFactory")
    public void edit(@Payload PostDTO post) {
        log.info("========== START EDITING POST ==========");
        PostDocument document = convertPostDTOtoPostDocument(post);
        postElasticsearchService.edit(document);
        log.info("========== END EDITING POST ==========");
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
