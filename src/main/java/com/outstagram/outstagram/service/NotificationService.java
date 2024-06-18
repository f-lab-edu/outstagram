package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.dto.AlarmType;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.NotificationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    private final UserService userService;

    private final ImageService imageService;

    private final SqlSessionFactory sqlSessionFactory;

    public void insertNotification(NotificationDTO notification) {
        notificationMapper.insertNotification(notification);
    }

    public List<NotificationDetailsDTO> getNotificationDetailsPlusOne(Long userId, Long lastId) {
        List<NotificationDTO> notificationList = notificationMapper.findByUserIdAndLastId(userId,
            lastId, PAGE_SIZE + 1);

        return notificationList.stream()
            .map(dto -> {
                UserDTO user = userService.getUser(dto.getFromId());
                ImageDTO image = null;
                if (dto.getAlarmType() != AlarmType.FOLLOW) {
                    image = imageService.getImageInfos(dto.getTargetId()).get(0);
                }

                return NotificationDetailsDTO.builder()
                    .id(dto.getId())
                    .fromId(dto.getFromId())
                    .fromNickname(user.getNickname())
                    .fromImgUrl(user.getImgUrl())
                    .targetId(dto.getTargetId())
                    .isRead(dto.getIsRead())
                    .alarmType(dto.getAlarmType())
                    .createDate(dto.getCreateDate())
                    .postImgUrl(image.getImgUrl())
                    .build();
            })
            .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public NotificationDTO readNotification(Long notiId, Long userId) {
        // 알림 존재 여부 확인
        NotificationDTO notification = notificationMapper.findByIdAndUserId(notiId, userId);
        if (notification == null) {
            throw new ApiException(ErrorCode.NOT_FOUND_NOTIFICATION);
        }

        // 알림 읽음 처리
        if (!notification.getIsRead()) {
            notificationMapper.readNotification(notiId, userId);
            notification.setIsRead(true);
        }

        return notification;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void readAllNotification(Long userId) {
        // 해당 유저의 모든 알림 가져와서
        List<NotificationDTO> notificationList = notificationMapper.findByUserIdAndLastId(userId,
            null, Integer.MAX_VALUE);

        // 모든 알림 읽음 처리(update 쿼리 모아서 배치 처리)
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            NotificationMapper mapper = session.getMapper(NotificationMapper.class);
            notificationList.forEach(noti -> mapper.readNotification(noti.getId(), userId));
            session.commit();
        }

    }
}
