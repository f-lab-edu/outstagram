package com.outstagram.outstagram.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditUserReq {
    @NotBlank(message = "닉네임은 필수 입력입니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    private String password;

}
