package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.LikeDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMapper {

    void insertLike(LikeDTO like);

    boolean existsUserLike(Long userId, Long postId);

    int deleteLike(Long userId, Long postId);

    List<Long> findPostIdsByUserId(Long userId);
}
