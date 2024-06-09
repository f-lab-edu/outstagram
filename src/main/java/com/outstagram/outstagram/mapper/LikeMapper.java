package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMapper {

    void insertLike(LikeDTO like);

    boolean existsUserLike(Long userId, Long postId);

    int deleteLike(Long userId, Long postId);

    int deleteByPostId(Long postId);

    List<PostImageDTO> findWithPostsAndImageByUserId(Long userId, Long lastId, int size);

    List<Long> findIdsByUserId(Long userId, Long lastId, int size);

    int insertLikeAll(List<LikeDTO> insertLikeList);
}
