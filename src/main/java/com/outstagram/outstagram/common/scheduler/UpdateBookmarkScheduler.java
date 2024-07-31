package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.config.database.DataSourceContextHolder;
import com.outstagram.outstagram.dto.BookmarkDTO;
import com.outstagram.outstagram.dto.BookmarkRecordDTO;
import com.outstagram.outstagram.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.INSERT_BOOKMARK_LOCK;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_BOOKMARK_PREFIX;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UpdateBookmarkScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookmarkService bookmarkService;

    /**
     * like 테이블에 좋아요 기록 insert
     */
    @SchedulerLock(name = INSERT_BOOKMARK_LOCK, lockAtLeastFor = "10s", lockAtMostFor = "20s")
    @Scheduled(cron = "0 */27 * * * ?") // 매 7분마다 실행
    public void insertBookmarks() {
        log.info("=================== 북마크 정보 DB에 insert 시작");

        Set<String> userBookmarkKeys = redisTemplate.keys(USER_BOOKMARK_PREFIX + "*");
        if (userBookmarkKeys != null) {
            HashMap<Long, ArrayList<BookmarkDTO>> insertList = new HashMap<>();
            List<String> deleteKeys = new ArrayList<>();
            for (String key : userBookmarkKeys) {
                Long userId = Long.parseLong(key.replace(USER_BOOKMARK_PREFIX, ""));

                // insert할 bookmark 정보 모으기 (shardId 별로 모으기)
                redisTemplate.opsForList()
                        .range(key, 0, -1)
                        .stream()
                        .map(record -> (BookmarkRecordDTO) record)
                        .forEach(record -> {
                            Long postId = record.getPostId();
                            Integer shardId = (Integer) redisTemplate.opsForValue().get(postId.toString());
                            BookmarkDTO bookmark = new BookmarkDTO(userId, record.getPostId(),
                                    record.getBookmarkAt());
                            if (insertList.containsKey(shardId)) {
                                ArrayList<BookmarkDTO> bookmarks = insertList.get(shardId);
                                bookmarks.add(bookmark);
                                insertList.put(Long.valueOf(shardId), bookmarks);
                            } else {
                                ArrayList<BookmarkDTO> list = new ArrayList<>();
                                list.add(bookmark);
                                insertList.put(Long.valueOf(shardId), list);
                            }
                        });

                // 삭제할 키 모으기
                deleteKeys.add(key);
            }

            for (long shardId = 0; shardId < DB_COUNT; shardId++) {
                log.info("=================== {}번 shard에 북마크 정보 insert", shardId);
                ArrayList<BookmarkDTO> list = insertList.get(shardId);
                DataSourceContextHolder.setShardId(shardId);
                bookmarkService.insertBookmarkAll(list);
                DataSourceContextHolder.clearShardId();
            }

            redisTemplate.delete(deleteKeys);
        }
        log.info("=================== 북마크 정보 DB에 insert 종료");
    }

}
