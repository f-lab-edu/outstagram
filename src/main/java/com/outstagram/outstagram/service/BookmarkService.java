package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.CacheConst.IN_CACHE;
import static com.outstagram.outstagram.common.constant.CacheConst.IN_DB;
import static com.outstagram.outstagram.common.constant.CacheConst.NOT_FOUND;
import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_BOOKMARK_PREFIX;

import com.outstagram.outstagram.dto.BookmarkDTO;
import com.outstagram.outstagram.dto.BookmarkRecordDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.BookmarkMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    public void insertBookmark(Long userId, Long postId, LocalDateTime time) {
        BookmarkDTO newBookmark = BookmarkDTO.builder()
            .userId(userId)
            .postId(postId)
            .createDate(time)
            .build();

        try {
            bookmarkMapper.insertBookmark(newBookmark);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DUPLICATED_BOOKMARK);
        } catch (Exception e) {
            throw new ApiException(e, ErrorCode.INSERT_ERROR);
        }
    }

    public void insertBookmarkAll(List<BookmarkDTO> bookmarkList) {
        if (bookmarkList != null && !bookmarkList.isEmpty()) {
            try {
                bookmarkMapper.insertBookmarkAll(bookmarkList);
            } catch (DuplicateKeyException e) {
                throw new ApiException(ErrorCode.DUPLICATED_BOOKMARK);
            } catch (Exception e) {
                throw new ApiException(e, ErrorCode.INSERT_ERROR);
            }
        }
    }

    /**
     * 캐시에 있음 -> 2
     * DB에 있음 -> 1
     * 없음 -> 0
     */
    public int existsBookmark(Long userId, Long postId) {
        String userBookmarkKey = USER_BOOKMARK_PREFIX + userId;

        List<Object> likedPost = redisTemplate.opsForList().range(userBookmarkKey, 0, -1);
        boolean isBookmarkRecordInCache = likedPost.stream()
            .map(record -> (BookmarkRecordDTO) record)
            .anyMatch(record -> record.getPostId().equals(postId));

        // 캐시에 북마크 누른 기록 있을 때
        if (isBookmarkRecordInCache) {
            return IN_CACHE;
        }

        // 캐시에 없어서 DB 조회
        return bookmarkMapper.existsUserBookmark(userId, postId) ? IN_DB : NOT_FOUND;
    }

    public void deleteBookmark(Long userId, Long postId) {
        int result = bookmarkMapper.deleteBookmark(userId, postId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR);
        }
    }

    public void deleteBookmarkAll(List<BookmarkDTO> deleteBookmarkList) {
        if (deleteBookmarkList != null && !deleteBookmarkList.isEmpty()) {
            int result = bookmarkMapper.deleteBookmarkAll(deleteBookmarkList);
            if (result == 0) {
                throw new ApiException(ErrorCode.DELETE_ERROR);
            }
        }

    }

    public List<PostImageDTO> getBookmarkedPosts(Long userId, Long postId) {
        return bookmarkMapper.findWithPostsAndImageByUserId(userId, postId, PAGE_SIZE);
    }

    public List<Long> getBookmarkedPostIds(Long userId, Long postId, int size) {
        return bookmarkMapper.findIdsByUserId(userId, postId, size);
    }
}
