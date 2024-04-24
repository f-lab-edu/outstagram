package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.LikeMapper;
import java.time.LocalDateTime;
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
        int result = likeMapper.insertLike(newLike);
        if (result == 0) {
            throw new ApiException(ErrorCode.INSERT_ERROR);
        }
    }

    public Boolean existsLike(Long userId, Long postId) {
        return likeMapper.existsUserLike(userId, postId);
    }

}
