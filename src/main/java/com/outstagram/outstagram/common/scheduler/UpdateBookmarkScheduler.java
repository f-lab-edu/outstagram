package com.outstagram.outstagram.common.scheduler;

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
import java.util.List;
import java.util.Set;

import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.INSERT_LOCK;
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
    @SchedulerLock(name = INSERT_LOCK, lockAtLeastFor = "10s", lockAtMostFor = "20s")
    @Scheduled(cron = "0 */7 * * * ?") // 매 7분마다 실행
    public void insertBookmarks() {
        log.info("=================== 북마크 정보 DB에 insert 시작");

        Set<String> userBookmarkKeys = redisTemplate.keys(USER_BOOKMARK_PREFIX + "*");
        if (userBookmarkKeys != null) {
            List<BookmarkDTO> insertBookmarkList = new ArrayList<>();
            List<String> deleteKeys = new ArrayList<>();
            for (String key : userBookmarkKeys) {
                Long userId = Long.parseLong(key.replace(USER_BOOKMARK_PREFIX, ""));

                // insert할 bookmark 정보 모으기
                redisTemplate.opsForList()
                        .range(key, 0, -1)
                        .stream()
                        .map(record -> (BookmarkRecordDTO) record)
                        .map(record -> new BookmarkDTO(userId, record.getPostId(),
                                record.getBookmarkAt()))
                        .forEach(insertBookmarkList::add);

                // 삭제할 키 모으기
                deleteKeys.add(key);
            }

            bookmarkService.insertBookmarkAll(insertBookmarkList);

            redisTemplate.delete(deleteKeys);
        }
        log.info("=================== 북마크 정보 DB에 insert 종료");
    }

}
