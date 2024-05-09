package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.response.FollowRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse> following(@PathVariable("userId") Long toId, @Login UserDTO user) {
        followService.addFollowing(user.getId(), toId);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("팔로우에 성공했습니다.")
                .build()
        );
    }

    @GetMapping("/following")
    public ResponseEntity<List<FollowRes>> getFollowingList(@Login UserDTO user) {
        List<FollowRes> response = followService.getFollowingList(user.getId());

        return ResponseEntity.ok(response);
    }


}
