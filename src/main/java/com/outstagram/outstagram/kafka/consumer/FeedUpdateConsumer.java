package com.outstagram.outstagram.kafka.consumer;

import static com.outstagram.outstagram.common.constant.FeedConst.FEED_MAX_SIZE;
import static com.outstagram.outstagram.common.constant.KafkaConst.FEED_GROUPID;
import static com.outstagram.outstagram.common.constant.KafkaConst.FEED_TOPIC;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.FEED;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.FOLLOWER;

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
    @KafkaListener(topics = FEED_TOPIC, groupId = FEED_GROUPID, containerFactory = "feedKafkaListenerContainerFactory")
    public void receive(ConsumerRecord<String, Long> consumerRecord) {
        Long userId = Long.parseLong(consumerRecord.key());
        Long postId = consumerRecord.value();
        log.info("=========== received userID = {}, postID = {}", userId, postId);

        // Redis에서 userId의 팔로워 ID 목록 가져오기
        Set<Object> followerIds = redisTemplate.opsForSet().members(FOLLOWER + userId);
        if (followerIds == null) {
            log.info("====================== userID {}는 팔로워가 없습니다.", userId);
            return;
        }

        log.info("=========== followerIds = {}", followerIds);

        // 내 피드 목록에도 내가 생성한 postId 넣기
        redisTemplate.opsForList().leftPush(FEED + userId, postId);
        redisTemplate.opsForList().trim(FEED + userId, 0, FEED_MAX_SIZE - 1);

        // 각 팔로워의 피드목록에 postId 넣기
        followerIds.forEach(
                id -> {
                    String feedKey = FEED + id;
                    redisTemplate.opsForList().leftPush(feedKey, postId);
                    redisTemplate.opsForList().trim(feedKey, 0, FEED_MAX_SIZE - 1);
                });

        log.info("=========== feed push success!");

    }


}
