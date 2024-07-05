package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.LikeCountDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostImageDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMapper {

    int insertPost(PostDTO post);

    PostDTO findById(Long postId);

    int updateContentsById(Long postId, String contents);

    int deleteById(Long postId);

    List<PostImageDTO> findWithImageByUserId(Long userId, Long lastId, int size);

    List<Long> findIdsByUserId(Long userId, Long lastId, int size);

    int updateLikeCount(Long postId, int count, int currentVersion);

    void updateLikeCountAll(List<LikeCountDTO> likeCountDTOList);

    List<Long> getFeedIdsFromDB(Long userId, Long lastId, int size);

    List<PostDTO> findByKeyword(String search);
}
