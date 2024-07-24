package com.outstagram.outstagram.kafka.consumer;

import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.CommentService;
import com.outstagram.outstagram.service.NotificationService;
import com.outstagram.outstagram.service.PostService;
import com.outstagram.outstagram.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.outstagram.outstagram.common.constant.KafkaConst.NOTIFICATION_GROUPID;
import static com.outstagram.outstagram.common.constant.KafkaConst.SEND_NOTIFICATION;
import static com.outstagram.outstagram.dto.AlarmType.COMMENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    private final PostService postService;

    private final CommentService commentService;

    private final SnowflakeIdGenerator idGenerator;
    
    // TODO : toId에게 이메일 보내는걸로 리팩토링
    @KafkaListener(topics = SEND_NOTIFICATION, groupId = NOTIFICATION_GROUPID, containerFactory = "notificationKafkaListenerContainerFactory")
    public void receive(@Payload NotificationDTO notification) {
        log.info("=========== send notification start!");

        List<NotificationDTO> notificationsToSend = new ArrayList<>(2);

        switch (notification.getAlarmType()) {
            case COMMENT, LIKE -> {
                PostDTO post = postService.getPost(notification.getTargetId());
                if (!post.getUserId().equals(notification.getFromId())) {
                    notification.setToId(post.getUserId());
                    notificationsToSend.add(notification);
                }
            }

            case REPLY -> {
                // 댓글 주인에게 대댓글 알림 보내기
                CommentDTO comment = commentService.findById(notification.getTargetId());
                // 자기 댓글에는 알림X
                if (!comment.getUserId().equals(notification.getFromId())) {
                    notification.setToId(comment.getUserId());
                    notificationsToSend.add(notification);
                }
                // 게시물 주인에게 댓글 알림 보내기(자기 게시물에는 알림X)
                PostDTO post = postService.getPost(comment.getPostId());
                if (!post.getUserId().equals(notification.getFromId())) {
                    notification.setToId(post.getUserId());
                    notification.setTargetId(comment.getPostId());
                    notification.setAlarmType(COMMENT);
                    notificationsToSend.add(notification);
                }
            }

            case FOLLOW -> {
                if (!notification.getTargetId().equals(notification.getFromId())) {
                    notification.setToId(notification.getTargetId());
                    notificationsToSend.add(notification);
                }
            }

            default -> throw new ApiException(ErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        for (NotificationDTO noti : notificationsToSend) {
            long notiId = idGenerator.snowflakeIdGenerator(noti.getFromId());
            noti.setId(notiId);
            notificationService.insertNotification(noti);
            log.info("Notification[{}] sent: from user {} to user {}, type: {}\n", noti.getId(), noti.getFromId(), noti.getToId(), noti.getAlarmType());
        }

        log.info("=========== send notification success!");
    }
}
