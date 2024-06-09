package com.outstagram.outstagram.common.scheduler;

import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_BOOKMARK_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_UNBOOKMARK_PREFIX;

import com.outstagram.outstagram.dto.BookmarkDTO;
import com.outstagram.outstagram.dto.BookmarkRecordDTO;
import com.outstagram.outstagram.service.BookmarkService;
import java.util.ArrayList;
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
public class UpdateBookmarkScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BookmarkService bookmarkService;

    /**
     * like 테이블에 좋아요 기록 insert
     */
    @Transactional
    @Scheduled(fixedRate = 450000)
    public void insertBookmarks() {
        log.info("=================== 북마크 정보 DB에 insert 시작");

        Set<String> userBookmarkKeys = redisTemplate.keys(USER_BOOKMARK_PREFIX + "*");
        if (userBookmarkKeys != null) {
            List<BookmarkDTO> insertBookmarkList = new ArrayList<>();
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

                // 캐시에서 해당 키 삭제
                redisTemplate.delete(key);
            }

            // 한번에 db에 insert
            bookmarkService.insertBookmarkAll(insertBookmarkList);
        }
        log.info("=================== 북마크 정보 DB에 insert 종료");
    }

    @Transactional
    @Scheduled(fixedRate = 450000)
    public void deleteBookmarks() {
        log.info("=================== 북마크 정보 DB에서 delete 시작");

        Set<String> userUnbookmarkKeys = redisTemplate.keys(USER_UNBOOKMARK_PREFIX + "*");
        if (userUnbookmarkKeys != null) {
            List<BookmarkDTO> deleteBookmarkList = new ArrayList<>();

            for (String key : userUnbookmarkKeys) {
                Long userId = Long.parseLong(key.replace(USER_UNBOOKMARK_PREFIX, ""));

                redisTemplate.opsForSet().members(key).stream()
                    .map(Object::toString)
                    .map(Long::parseLong)
                    .map(postId -> new BookmarkDTO(userId, postId))
                    .forEach(deleteBookmarkList::add);

                // 캐시에서 해당 키 삭제
                redisTemplate.delete(key);
            }

            // 한번에 db에서 delete
            bookmarkService.deleteBookmarkAll(deleteBookmarkList);
        }
        log.info("=================== 북마크 정보 DB에서 delete 종료");

    }
}
