package com.outstagram.outstagram.mapper;

import org.springframework.stereotype.Repository;

@Repository
public interface FollowMapper {

    void insertFollow(Long fromId, Long toId);
}
