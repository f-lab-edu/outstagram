package com.outstagram.consumer.consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Data
@Slf4j
public class FeedUpdateConsumer {

    private CountDownLatch latch = new CountDownLatch(10);
    private List<Long> payloads = new ArrayList<>();
    private Long payload;

    // consumer 설정
//    @KafkaListener(topics = "feed", groupId = "sns-feed")
//    public void receive(ConsumerRecord<String, Long> consumerRecord) {
//        payload = consumerRecord.value();
//        log.info("received payload = {}", payload.toString());
//        payloads.add(payload);
//        latch.countDown();
//    }

    @KafkaListener(topics = "feed", groupId = "sns-feed")
    public void receive(Long postId) {
        log.info("=========== received postID = {}", postId);
        payloads.add(payload);
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
