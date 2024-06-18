package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.NotificationDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationMapper {

    void insertNotification(NotificationDTO notification);

    List<NotificationDTO> findByUserIdAndLastId(Long userId, Long lastId, int size);

    void readNotification(Long notiId, Long userId);

    NotificationDTO findByIdAndUserId(Long notiId, Long userId);
}
