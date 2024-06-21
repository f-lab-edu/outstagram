package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyNotificationsRes {
    private List<NotificationDetailsDTO> notificationList;
    private boolean hasNext;
}
