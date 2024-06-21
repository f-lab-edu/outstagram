package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.response.MyNotificationsRes;
import com.outstagram.outstagram.controller.response.UserInfoRes;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import com.outstagram.outstagram.dto.PostDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.NotificationService;
import com.outstagram.outstagram.service.PostService;
import com.outstagram.outstagram.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private final UserService userService;
    private final PostService postService;

    /**
     * 내 알림 목록 최신 순으로 조회
     */
    @GetMapping
    public ResponseEntity<MyNotificationsRes> getNotifications(
            @RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<NotificationDetailsDTO> response = notificationService.getNotificationDetailsPlusOne(
                user.getId(), lastId);

        boolean hasNext = response.size() > PAGE_SIZE;
        if (hasNext) {
            response = response.subList(0, PAGE_SIZE);
        }
        return ResponseEntity.ok(MyNotificationsRes.builder()
                .notificationList(response)
                .hasNext(hasNext)
                .build());
    }

    /**
     * 알림 단건 읽음 처리
     */
    @PatchMapping("/{notiId}")
    public ResponseEntity<Object> readNotification(@PathVariable Long notiId,
                                                   @Login UserDTO user) {
        NotificationDTO notification = notificationService.readNotification(notiId,
                user.getId());

        switch (notification.getAlarmType()) {
            // 팔로우 알림인 경우 -> 나를 팔로우한 유저 정보 리턴
            case FOLLOW -> {
                UserDTO dto = userService.getUser(notification.getFromId());

                UserInfoRes response = UserInfoRes.builder()
                        .userId(dto.getId())
                        .nickname(dto.getNickname())
                        .email(dto.getEmail())
                        .imgUrl(dto.getImgUrl())
                        .build();
                return ResponseEntity.ok(response);
            }
            // 좋아요, 댓글, 대댓글 알림인 경우 -> 해당 게시물 정보 리턴
            case LIKE, COMMENT, REPLY -> {
                PostDetailsDTO postDetailsDTO = postService.getPostDetails(notification.getTargetId(),
                        user.getId());

                return ResponseEntity.ok(postDetailsDTO);
            }
            default -> throw new ApiException(ErrorCode.INVALID_NOTIFICATION_TYPE);
        }
    }

    /**
     * 알림 모두 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse> readAllNotification(@Login UserDTO user) {
        notificationService.readAllNotification(user.getId());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("모든 알림을 읽음 처리 했습니다.")
                        .httpStatus(HttpStatus.OK)
                        .isSuccess(true)
                        .build()
        );
    }

}
