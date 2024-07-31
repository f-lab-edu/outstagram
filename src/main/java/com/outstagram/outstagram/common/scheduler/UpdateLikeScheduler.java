package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.config.database.DataSourceContextHolder;
import com.outstagram.outstagram.dto.LikeCountDTO;
import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.dto.LikeRecordDTO;
import com.outstagram.outstagram.service.LikeService;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;
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
    @Scheduled(cron = "0 */47 * * * ?") // 매 5분마다 실행
    public void updateLikes() {
        log.info("=================== 좋아요 개수 DB에 반영 시작");
        // likeCount:{postId} 전부 가져오기
        Set<String> keys = redisTemplate.keys(LIKE_COUNT_PREFIX + "*");

        if (keys != null) {
            Map<Long, ArrayList<LikeCountDTO>> likeCountMapList = new HashMap<>();

            for (String key : keys) {
                Long postId = Long.parseLong(key.replace(LIKE_COUNT_PREFIX, ""));
                Integer shardId = (Integer) redisTemplate.opsForValue().get(postId.toString());
                Integer likeCount = (Integer) redisTemplate.opsForValue().get(key);
                if (likeCount != null) {
                    // DB에 업데이트할 거 모으기
                    if (likeCountMapList.containsKey(shardId)) {
                        ArrayList<LikeCountDTO> likeCountList = likeCountMapList.get(shardId);
                        likeCountList.add(new LikeCountDTO(postId, likeCount));
                        likeCountMapList.put(Long.valueOf(shardId), likeCountList);
                    } else {
                        ArrayList<LikeCountDTO> likeCountList = new ArrayList<>();
                        likeCountList.add(new LikeCountDTO(postId, likeCount));
                        likeCountMapList.put(Long.valueOf(shardId), likeCountList);
                    }
                }
            }

            // 한번에 DB에 업데이트하기
            for (long shardId = 0; shardId < DB_COUNT; shardId++) {
                log.info("=================== {}번 shard에 좋아요 개수 반영하기", shardId);
                ArrayList<LikeCountDTO> list = likeCountMapList.get(shardId);
                DataSourceContextHolder.setShardId(shardId);
                postService.updateLikeCountAll(list);
                DataSourceContextHolder.clearShardId();
            }
            log.info("=================== 좋아요 개수 DB에 반영 종료");
        }
    }

    /**
     * like 테이블에 좋아요 기록 insert
     */
    @SchedulerLock(name = INSERT_USERLIKE_LOCK, lockAtLeastFor = "10s", lockAtMostFor = "50s")
    @Scheduled(cron = "0 */52 * * * ?") // 매 6분마다 실행
    public void insertUserLike() {
        log.info("=================== 좋아요 정보 DB에 insert 시작");

        Set<String> userLikeKeys = redisTemplate.keys(USER_LIKE_PREFIX + "*");
        if (userLikeKeys != null) {
            HashMap<Long, ArrayList<LikeDTO>> insertLikeMapList = new HashMap<>();
            List<String> deleteKeys = new ArrayList<>();

            for (String key : userLikeKeys) {
                Long userId = Long.parseLong(key.replace(USER_LIKE_PREFIX, ""));

                redisTemplate.opsForList().range(key, 0, -1)
                        .stream()
                        .map(record -> (LikeRecordDTO) record)
                        .forEach(record -> {
                            Long postId = record.getPostId();
                            Integer shardId = (Integer) redisTemplate.opsForValue().get(postId.toString());
                            LikeDTO like = new LikeDTO(userId, record.getPostId(), record.getLikeAt());
                            if (insertLikeMapList.containsKey(shardId)) {
                                ArrayList<LikeDTO> likes = insertLikeMapList.get(shardId);
                                likes.add(like);
                                insertLikeMapList.put(Long.valueOf(shardId), likes);
                            } else {
                                ArrayList<LikeDTO> list = new ArrayList<>();
                                list.add(like);
                                insertLikeMapList.put(Long.valueOf(shardId), list);
                            }
                        });


                deleteKeys.add(key);
            }

            for (long shardId = 0; shardId < DB_COUNT; shardId++) {
                log.info("=================== {}번 shard에 좋아요 정보 insert", shardId);
                ArrayList<LikeDTO> list = insertLikeMapList.get(shardId);
                DataSourceContextHolder.setShardId(shardId);
                likeService.insertLikeAll(list);
                DataSourceContextHolder.clearShardId();
            }

            redisTemplate.delete(deleteKeys);
        }

        log.info("=================== 좋아요 정보 DB에 insert 종료");
    }

}
