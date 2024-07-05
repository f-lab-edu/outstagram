package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.dto.BookmarkDTO;
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

    public boolean existsBookmark(Long userId, Long postId) {
        return bookmarkMapper.existsUserBookmark(userId, postId);
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
