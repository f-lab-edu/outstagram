package com.outstagram.outstagram.kafka.consumer;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedUpdateConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    // consumer 설정
    @KafkaListener(topics = "feed", groupId = "sns-feed", containerFactory = "feedKafkaListenerContainerFactory")
    public void receive(ConsumerRecord<String, Long> consumerRecord) {
        Long userId = Long.parseLong(consumerRecord.key());
        Long postId = consumerRecord.value();
        log.info("=========== received userID = {}, postID = {}", userId, postId);

        // Redis에서 userId의 팔로워 ID 목록 가져오기
        Set<Object> followerIds = redisTemplate.opsForSet().members("followers:" + userId);
        if (followerIds == null) {
            log.error("====================== userID {}는 팔로워가 없습니다.", userId);
            return;
        }

        log.info("=========== followerIds = {}", followerIds);

        // 각 팔로워의 피드목록에 postId 넣기
        followerIds.forEach(
            id -> {
                String feedKey = "feed:" + id;
                redisTemplate.opsForList().leftPush(feedKey, postId);
            });
        log.info("=========== feed push success!");

    }


}
