package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.BookmarkDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkMapper {

    void insertBookmark(BookmarkDTO like);

    boolean existsUserBookmark(Long userId, Long postId);

    int deleteBookmark(Long userId, Long postId);

    List<Long> findPostIdsByUserId(Long userId);
}
