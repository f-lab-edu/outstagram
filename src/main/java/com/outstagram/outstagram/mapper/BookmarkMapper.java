package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.BookmarkDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkMapper {

    void insertBookmark(BookmarkDTO bookmark);

    boolean existsUserBookmark(Long userId, Long postId);

    int deleteBookmark(Long userId, Long postId);

    List<PostImageDTO> findWithPostsAndImageByUserId(Long userId, Long lastId, int size);
}
