package com.outstagram.outstagram.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Builder
@Data
public class ApiResponse {

    private String message;
    private HttpStatus httpStatus;
    private Boolean isSuccess;
}
