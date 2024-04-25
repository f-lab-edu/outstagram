package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.LikeMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;

    public void insertLike(Long userId, Long postId) {
        LikeDTO newLike = LikeDTO.builder()
            .userId(userId)
            .postId(postId)
            .createDate(LocalDateTime.now())
            .build();

        likeMapper.insertLike(newLike);
    }

    public Boolean existsLike(Long userId, Long postId) {
        return likeMapper.existsUserLike(userId, postId);
    }

    public void deleteLike(Long userId, Long postId) {
        int result = likeMapper.deleteLike(userId, postId);
        if (result == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR);
        }
    }

    public List<Long> getLikePosts(Long userId) {
        return likeMapper.findPostIdsByUserId(userId);
    }
}
