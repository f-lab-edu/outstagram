package com.outstagram.outstagram.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO implements Serializable {

    @Id
    private Long id;

    private AlarmType alarmType;

    // 알림을 보낸 유저
    private Long fromId;

    // 알림을 받을 유저
    private Long toId;

    // 좋아요, 댓글 -> 게시물 id, 팔로우 -> 팔로우한 유저 id
    private Long targetId;

    private Boolean isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

}
