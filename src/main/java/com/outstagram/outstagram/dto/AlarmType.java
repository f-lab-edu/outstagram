package com.outstagram.outstagram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlarmType {
    LIKE,
    COMMENT,
    FOLLOW,
    REPLY
}
