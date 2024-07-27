package com.outstagram.outstagram.common.interceptor;


import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;

import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse
        response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("인증 체크 인터셉터 실행 {}", requestURI);
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(LOGIN_USER) == null) {
            log.info("미인증 사용자 요청");
            throw new ApiException(ErrorCode.UNAUTHORIZED_USER);
        }
        return true;
    }
}

