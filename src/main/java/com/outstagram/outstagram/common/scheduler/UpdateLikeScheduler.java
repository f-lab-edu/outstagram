package com.outstagram.outstagram.common.scheduler;

import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.LIKE_COUNT_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_LIKE_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_UNLIKE_PREFIX;

import com.outstagram.outstagram.dto.LikeRecordDTO;
import com.outstagram.outstagram.mapper.PostMapper;
import com.outstagram.outstagram.service.LikeService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UpdateLikeScheduler {

    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeService likeService;

    /**
     * 게시물에 좋아요 개수 반영
     */
    @Transactional
    @Scheduled(fixedRate = 300000)  // 5분마다 실행
    public void updateLikes() {
        log.info("=================== 좋아요 개수 DB에 반영 시작");
        // likeCount:{postId} 전부 가져오기
        Set<String> keys = redisTemplate.keys(LIKE_COUNT_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                Long postId = Long.parseLong(key.replace(LIKE_COUNT_PREFIX, ""));
                Integer likeCount = (Integer) redisTemplate.opsForValue().get(key);
                if (likeCount != null) {
                    postMapper.updateLikeCount(postId, likeCount);

                    // 캐시에서 해당 키 삭제
                    redisTemplate.delete(key);
                }
            }
        }
        log.info("=================== 좋아요 개수 DB에 반영 종료");
    }

    /**
     * like 테이블에 좋아요 기록 insert
     */
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void insertUserLike() {
        log.info("=================== 좋아요 정보 DB에 insert 시작");

        Set<String> userLikeKeys = redisTemplate.keys(USER_LIKE_PREFIX + "*");
        if (userLikeKeys != null) {
            for (String key : userLikeKeys) {
                Long userId = Long.parseLong(key.replace(USER_LIKE_PREFIX, ""));
                List<LikeRecordDTO> likeRecordList = redisTemplate.opsForList().range(key, 0, -1)
                    .stream()
                    .map(record -> (LikeRecordDTO) record)
                    .toList();

                likeRecordList
                    .forEach(record -> likeService.insertLike(userId, record.getPostId(),
                        record.getLikeAt()));

                // 캐시에서 해당 키 삭제
                redisTemplate.delete(key);
            }
        }
        log.info("=================== 좋아요 정보 DB에 insert 종료");
    }

    @Transactional
    @Scheduled(fixedRate = 300000)
    public void deleteUserLike() {
        log.info("=================== 좋아요 정보 DB에서 delete 시작");

        Set<String> userUnlikeKeys = redisTemplate.keys(USER_UNLIKE_PREFIX + "*");
        if (userUnlikeKeys != null) {
            for (String key : userUnlikeKeys) {
                Long userId = Long.parseLong(key.replace(USER_UNLIKE_PREFIX, ""));
                Set<Object> postIds = redisTemplate.opsForSet().members(key);
                if (postIds != null) {
                    for (Object postIdObj : postIds) {
                        Long postId = ((Integer) postIdObj).longValue();
                        likeService.deleteLike(userId, postId);
                    }
                    // 캐시에서 해당 키 삭제
                    redisTemplate.delete(key);
                }
            }
        }
        log.info("=================== 좋아요 정보 DB에서 delete 종료");

    }
}
