package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.dto.NotificationDetailsDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyNotificationsRes {
    private List<NotificationDetailsDTO> notificationList;
    private boolean hasNext;


}
