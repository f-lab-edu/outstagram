package com.outstagram.outstagram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedUpdateService {
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "postCreatedTopic", groupId = "sns-feed")
    public void updateFeed(String postId) {

    }



}
