package com.outstagram.outstagram.kafka.producer;

import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostProducer {

    private final KafkaTemplate<String, PostDTO> postKafkaTemplate;

    public void save(String topic, PostDTO post) {
        log.info("========== SEND POST SAVE post : {}", post);
        postKafkaTemplate.send(topic, post);
    }

    public void edit(String topic, PostDTO post) {
        log.info("========== SEND POST EDIT post : {}", post);
        postKafkaTemplate.send(topic, post);
    }

    public void delete(String topic, PostDTO post) {
        log.info("========== SEND POST DELETE post : {}", post);
        postKafkaTemplate.send(topic, post);
    }
}
