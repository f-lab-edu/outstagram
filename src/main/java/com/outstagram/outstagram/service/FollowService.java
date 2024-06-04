package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.response.FollowRes;
import com.outstagram.outstagram.controller.response.UserInfoRes;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {
    private final static String FOLLOWING = "followings:";
    private final static String FOLLOWER = "followers:";

    private final UserService userService;

    private final FollowMapper followMapper;
    private final RedisTemplate<String, Object> redisTemplate;



    /**
     * 동기적으로 follow 목록 관리하기
     */
    public void addFollowing(Long fromId, Long toId) {
        try {
            followMapper.insertFollow(fromId, toId);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DUPLICATED_FOLLOW);
        }

        // TODO: kafka 활용해 비동기적으로 처리하기...?
        // fromId의 팔로잉 목록에 toId 추가
        String followingKey = makeFollowingKey(fromId);
        redisTemplate.opsForSet().add(followingKey, toId);

        // toId의 팔로워 목록에 fromId 추가
        String followersKey = makeFollowerKey(toId);
        redisTemplate.opsForSet().add(followersKey, fromId);
    }

    /**
     * 로그인 한 유저의 팔로잉 목록 가져오기(Redis에서)
     */
    public List<FollowRes> getFollowingList(Long userId) {
        // 유저의 following id 목록 가져오기
        Set<Object> memberId = redisTemplate.opsForSet().members(makeFollowingKey(userId));
        if (memberId == null) {
            return new ArrayList<>();
        }

        return getFollowResList(memberId);

    }

    /**
     * 로그인한 유저의 팔로워 목록 가져오기(Redis에서)
     */
    public List<FollowRes> getFollowerList(Long userId) {
        // 유저의 follower id 목록 가져오기
        Set<Object> memberId = redisTemplate.opsForSet().members(makeFollowerKey(userId));
        if (memberId == null) {
            return new ArrayList<>();
        }

        return getFollowResList(memberId);
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

    /**
     * id 목록으로 Redis에서 유저 정보 가져와 List<FollowRes>로 변환하기
     */
    private List<FollowRes> getFollowResList(Set<Object> memberId) {
        return memberId.stream()
            .map(id -> {
                Long userId;
                if (id instanceof Integer) {
                    userId = ((Integer) id).longValue();
                } else {
                    userId = (Long) id;
                }
                // getUser 메서드는 @Cacheable 선언되어 있어, 캐시 hit이면 캐시에서 miss면 db에서 가져옴
                UserInfoRes userInfo = userService.getUser(userId);
                log.info("==== userInfo : {}", userInfo);

                return FollowRes.builder()
                    .id(userInfo.getUserId())
                    .nickname(userInfo.getNickname())
                    .email(userInfo.getEmail())
                    .imgUrl(userInfo.getImgUrl())
                    .build();
            }).toList();
    }
}
