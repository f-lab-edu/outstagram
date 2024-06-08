package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_LIKE_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_UNLIKE_PREFIX;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.dto.LikeRecordDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.LikeMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    public void insertLike(Long userId, Long postId, LocalDateTime time) {
        LikeDTO newLike = LikeDTO.builder()
            .userId(userId)
            .postId(postId)
            .createDate(time)
            .build();

        try {
            likeMapper.insertLike(newLike);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DUPLICATED_LIKE);
        }
    }

    /**
     * 캐시에 좋아요 누른 기록 있으면 -> true
     * 캐시에 좋아요 취소한 기록 있음 -> false
     * 캐시에 좋아요 누른 기록도 없고 취소한 기록도 없음 -> DB 조회 결과 리턴
     *
     */
    public Boolean existsLike(Long userId, Long postId) {
        String userLikeKey = USER_LIKE_PREFIX + userId;
        String userUnlikeKey = USER_UNLIKE_PREFIX + userId;

        List<Object> likedPost = redisTemplate.opsForList().range(userLikeKey, 0, -1);
        boolean isDuplicate = likedPost.stream()
            .map(record -> (LikeRecordDTO) record)
            .anyMatch(record -> record.getPostId().equals(postId));
        // 캐시에 좋아요 누른 기록 있을 때
        if (isDuplicate) {
            return true;
        }

        // 캐시에 좋아요 취소한 기록 있을 때
        if (redisTemplate.opsForSet().isMember(userUnlikeKey, postId)) {
            return false;
        }

        // 캐시에 아무 기록 없음
        return likeMapper.existsUserLike(userId, postId);
    }

    public void deleteLike(Long userId, Long postId) {
        int result = likeMapper.deleteLike(userId, postId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR);
        }
    }

    public List<PostImageDTO> getLikePosts(Long userId, Long lastId) {
        return likeMapper.findWithPostsAndImageByUserId(userId, lastId, PAGE_SIZE);
    }

    public List<Long> getLikePostIds(Long userId, Long lastId, int size) {
        return likeMapper.findIdsByUserId(userId, lastId, size);
    }


}
