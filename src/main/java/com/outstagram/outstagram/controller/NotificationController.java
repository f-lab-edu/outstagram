package com.outstagram.outstagram.controller;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.controller.response.MyNotificationsRes;
import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<MyNotificationsRes> getNotifications(
        @RequestParam(required = false) Long lastId, @Login UserDTO user) {
        List<NotificationDetailsDTO> response = notificationService.getNotificationDetailsPlusOne(
            user.getId(), lastId);

        boolean hasNext = response.size() > PAGE_SIZE;
        return ResponseEntity.ok(MyNotificationsRes.builder()
            .notificationList(response)
            .hasNext(hasNext)
            .build());
    }


}
