package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.dto.LikeDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.LikeMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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

        try {
            likeMapper.insertLike(newLike);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DUPLICATED_LIKE);
        }
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

    public List<PostImageDTO> getLikePosts(Long userId, Long lastId) {
        return likeMapper.findWithPostsAndImageByUserId(userId, lastId, PAGE_SIZE);
    }
}
