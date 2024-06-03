package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoRes implements Serializable {
    private Long userId;
    private String nickname;
    private String email;
    private String imgUrl;
}
