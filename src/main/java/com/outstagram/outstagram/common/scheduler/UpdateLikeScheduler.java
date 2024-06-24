package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.dto.LikeCountDTO;
import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.dto.LikeRecordDTO;
import com.outstagram.outstagram.service.LikeService;
import com.outstagram.outstagram.service.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.*;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UpdateLikeScheduler {

    private final PostService postService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeService likeService;

    /**
     * 게시물에 좋아요 개수 반영
     */
    @Transactional
    @Scheduled(cron = "0 */5 * * * ?") // 매 5분마다 실행
    public void updateLikes() {
        log.info("=================== 좋아요 개수 DB에 반영 시작");
        // likeCount:{postId} 전부 가져오기
        Set<String> keys = redisTemplate.keys(LIKE_COUNT_PREFIX + "*");

        if (keys != null) {
            List<LikeCountDTO> likeCountList = new ArrayList<>();

            for (String key : keys) {
                Long postId = Long.parseLong(key.replace(LIKE_COUNT_PREFIX, ""));
                Integer likeCount = (Integer) redisTemplate.opsForValue().get(key);
                if (likeCount != null) {
                    // DB에 업데이트할 거 모으기
                    likeCountList.add(new LikeCountDTO(postId, likeCount));
                }
            }

            // 한번에 DB에 업데이트하기
            postService.updateLikeCountAll(likeCountList);
            log.info("=================== 좋아요 개수 DB에 반영 종료");
        }
    }

    /**
     * like 테이블에 좋아요 기록 insert
     */
    @SchedulerLock(name = INSERT_USERLIKE_LOCK, lockAtLeastFor = "10s", lockAtMostFor = "50s")
    @Scheduled(cron = "0 */6 * * * ?") // 매 6분마다 실행
    public void insertUserLike() {
        log.info("=================== 좋아요 정보 DB에 insert 시작");

        Set<String> userLikeKeys = redisTemplate.keys(USER_LIKE_PREFIX + "*");
        if (userLikeKeys != null) {
            List<LikeDTO> insertLikeList = new ArrayList<>();
            List<String> deleteKeys = new ArrayList<>();

            for (String key : userLikeKeys) {
                Long userId = Long.parseLong(key.replace(USER_LIKE_PREFIX, ""));

                insertLikeList.addAll(
                    redisTemplate.opsForList().range(key, 0, -1)
                        .stream()
                        .map(record -> (LikeRecordDTO) record)
                        .map(record -> new LikeDTO(userId, record.getPostId(), record.getLikeAt()))
                        .toList()
                );

                deleteKeys.add(key);
            }

            likeService.insertLikeAll(insertLikeList);
            redisTemplate.delete(deleteKeys);
        }

        log.info("=================== 좋아요 정보 DB에 insert 종료");
    }

}
