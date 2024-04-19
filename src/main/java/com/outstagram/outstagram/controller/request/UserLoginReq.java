package com.outstagram.outstagram.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserLoginReq {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
