package com.outstagram.outstagram.service;

import com.outstagram.outstagram.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;

    public void addFollowing(Long fromId, Long toId) {
        followMapper.insertFollow(fromId, toId);
    }
}
