package com.outstagram.outstagram.kafka.consumer;

import static com.outstagram.outstagram.common.constant.KafkaConst.NOTIFICATION_GROUPID;
import static com.outstagram.outstagram.common.constant.KafkaConst.SEND_NOTIFICATION;
import static com.outstagram.outstagram.dto.AlarmType.*;

import com.outstagram.outstagram.dto.AlarmType;
import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.service.CommentService;
import com.outstagram.outstagram.service.NotificationService;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    private final PostService postService;

    private final CommentService commentService;

    // TODO : toId에게 이메일 보내는걸로 리팩토링
    @KafkaListener(topics = SEND_NOTIFICATION, groupId = NOTIFICATION_GROUPID, containerFactory = "notificationKafkaListenerContainerFactory")
    public void receive(@Payload NotificationDTO notification) {
        log.info("=========== send notification start!");

        AlarmType alarmType = notification.getAlarmType();
        if (alarmType.equals(COMMENT)) {
            PostDTO post = postService.getPost(notification.getTargetId());
            notification.setToId(post.getUserId());
            // 알림 정보 DB에 저장하기
            notificationService.insertNotification(notification);

            log.info("user {} 님이 user {} 님의 post {} 에 댓글을 달았습니다.\n",
                notification.getFromId(), notification.getToId(), notification.getTargetId());
        } else if (alarmType.equals(LIKE)) {
            PostDTO post = postService.getPost(notification.getTargetId());
            notification.setToId(post.getUserId());
            // 알림 정보 DB에 저장하기
            notificationService.insertNotification(notification);

            log.info("user {} 님이 user {} 님의 post {} 에 좋아요를 눌렀습니다.\n",
                notification.getFromId(), notification.getToId(), notification.getTargetId())
        } else if (alarmType.equals(REPLY)) {
            // 댓글 주인에게 대댓글 알림 보내기
            CommentDTO comment = commentService.findById(notification.getTargetId());
            notification.setToId(comment.getUserId());
            notificationService.insertNotification(notification);
            log.info("user {} 님이 user {} 님의 댓글 {} 에 대댓글을 달았습니다.\n",
                notification.getFromId(), notification.getToId(), notification.getTargetId());

            // 게시물 주인에게 댓글 알림 보내기
            PostDTO post = postService.getPost(comment.getPostId());
            notification.setToId(post.getUserId());
            notification.setTargetId(comment.getPostId());
            notification.setAlarmType(COMMENT);
            notificationService.insertNotification(notification);
            log.info("user {} 님이 user {} 님의 post {} 에 댓글을 달았습니다.\n",
                notification.getFromId(), notification.getToId(), notification.getTargetId());
        } else if (alarmType.equals(FOLLOW)) {
            notification.setToId(notification.getTargetId());
            notificationService.insertNotification(notification);
            log.info("user {} 님이 user {} 님을 팔로우 했습니다.\n",
                notification.getFromId(), notification.getToId());
        }

        log.info("=========== send notification success!");

    }


}
