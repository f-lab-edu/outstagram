package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.LikeDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMapper {

    int insertLike(LikeDTO like);

    boolean existsUserLike(Long userId, Long postId);
}
