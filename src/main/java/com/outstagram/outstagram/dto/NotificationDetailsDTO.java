package com.outstagram.outstagram.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDetailsDTO {
    private Long id;

    private Long fromId;

    private String fromNickname;

    private String fromImgUrl;

    private Long targetId;

    private AlarmType alarmType;

    private LocalDateTime createDate;

    private String postImgUrl;

}
