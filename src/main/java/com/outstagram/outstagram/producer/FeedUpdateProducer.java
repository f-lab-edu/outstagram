package com.outstagram.outstagram.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedUpdateProducer {
    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void send(String topic, Long postId) {
        log.info("sending postId = {} to topic = {}", postId, topic);
        kafkaTemplate.send(topic, postId);
    }
}
