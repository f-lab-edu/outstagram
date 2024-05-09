package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.response.FollowRes;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
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
    private final static String FOLLOWING = "followings:";
    private final static String FOLLOWER = "followers:";

    private final FollowMapper followMapper;
    private final RedisTemplate<String, String> redisTemplate;


    /**
     * 동기적으로 follow 목록 관리하기
     */
    public void addFollowing(Long fromId, Long toId) {
        followMapper.insertFollow(fromId, toId);

        // TODO: kafka 활용해 비동기적으로 처리하기...?
        // fromId의 팔로잉 목록에 toId 추가
        String followingKey = makeFollowingKey(fromId);
        redisTemplate.opsForSet().add(followingKey, String.valueOf(toId));

        // toId의 팔로워 목록에 fromId 추가
        String followersKey = makeFollowerKey(toId);
        redisTemplate.opsForSet().add(followersKey, String.valueOf(fromId));
    }

    /**
     * 로그인 한 유저의 팔로잉 목록 가져오기(Redis에서)
     */
    public List<FollowRes> getFollowingList(Long userId) {
        // 유저의 following id 목록 가져오기
        Set<String> memberId = redisTemplate.opsForSet().members(makeFollowingKey(userId));
        if (memberId == null) {
            return new ArrayList<>();
        }

        // id 목록으로 Redis에서 유저 정보 가져와 List<FollowRes>로 변환하기
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

    public void deleteFollowing(Long fromId, Long toId) {
        // mysql에서 follow 기록 삭제
        int result = followMapper.deleteFollow(fromId, toId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR);
        }

        // fromId(나)의 팔로잉 목록에서 toId 삭제
        redisTemplate.opsForSet().remove(makeFollowingKey(fromId), String.valueOf(toId));
        // toId의 팔로워 목록에서 fromId 삭제
        redisTemplate.opsForSet().remove(makeFollowerKey(toId), String.valueOf(fromId));


    }

    private String makeFollowingKey(Long id) {
        return FOLLOWING + id;
    }

    private String makeFollowerKey(Long id) {
        return FOLLOWER + id;
    }
}
