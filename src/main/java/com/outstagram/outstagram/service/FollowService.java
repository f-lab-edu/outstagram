package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.response.FollowRes;
import com.outstagram.outstagram.mapper.FollowMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final RedisTemplate<String, String> redisTemplate;


    /**
     * 동기적으로 follow 목록 관리하기
     */
    public void addFollowing(Long fromId, Long toId) {
        followMapper.insertFollow(fromId, toId);

        // fromId의 팔로잉 목록에 toId 추가
        String followingKey = "followings:" + fromId;
        redisTemplate.opsForSet().add(followingKey, String.valueOf(toId));

        // toId의 팔로워 목록에 fromId 추가
        String followersKey = "followers:" + toId;
        redisTemplate.opsForSet().add(followersKey, String.valueOf(fromId));
    }

    public List<FollowRes> getFollowingList(Long userId) {
        // 유저의 following id 목록 가져오기
        Set<String> memberId = redisTemplate.opsForSet().members("followings:" + userId);
        if (memberId == null) {
            return new ArrayList<>();
        }

        // id 목록으로 유저 정보 가져오기
        return memberId.stream()
            .map(id -> {
                Map<Object, Object> userInfo = redisTemplate.opsForHash().entries("user:" + id);
                log.info("==== userInfo : {}" ,userInfo);
                return FollowRes.builder()
                    .id(Long.valueOf(id))
                    .nickname(String.valueOf(userInfo.get("nickname")))
                    .email(String.valueOf(userInfo.get("email")))
                    .imgUrl(String.valueOf(userInfo.get("img_url")))
                    .build();
            }).toList();

    }
}
