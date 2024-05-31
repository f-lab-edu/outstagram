package com.outstagram.outstagram.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowDTO {

    private Long fromId;

    private Long toId;

    private LocalDateTime createDate;

}
