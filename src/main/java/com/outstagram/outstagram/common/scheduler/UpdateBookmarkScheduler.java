package com.outstagram.outstagram.common.scheduler;

import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.INSERT_LOCK;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_BOOKMARK_PREFIX;

import com.outstagram.outstagram.dto.BookmarkDTO;
import com.outstagram.outstagram.dto.BookmarkRecordDTO;
import com.outstagram.outstagram.service.BookmarkService;
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
    // lockAtLeastFor : lock이 유지되는 최소 시간 (Job 의 수행 시간이 매우 빠를 때 중복 실행이 일어나는 문제를 방지하기 위해)
    // lockAtMostFor : lock이 유지되는 최대 시간 (Job 의 수행 시간이 매우 길어지거나, 끝나지 않을 때 다음 순서의 Job 이 실행되지 않는 문제를 방지하기 위해)
    @SchedulerLock(name = INSERT_LOCK, lockAtLeastFor = "10s", lockAtMostFor = "50s")
    @Scheduled(fixedRate = 450000)
    public void insertBookmarks() {
        log.info("=================== 북마크 정보 DB에 insert 시작");

        Set<String> userBookmarkKeys = redisTemplate.keys(USER_BOOKMARK_PREFIX + "*");
        if (userBookmarkKeys != null) {
            List<BookmarkDTO> insertBookmarkList = new ArrayList<>();
            List<String> deleteKeys= new ArrayList<>();
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
