package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;

import com.outstagram.outstagram.dto.AlarmType;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.NotificationDTO;
import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.mapper.NotificationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    private final UserService userService;

    private final ImageService imageService;

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
                    .isRead(dto.isRead())
                    .alarmType(dto.getAlarmType())
                    .createDate(dto.getCreateDate())
                    .postImgUrl(image.getImgUrl())
                    .build();
            })
            .toList();
    }
}
