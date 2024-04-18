package com.outstagram.outstagram.common.aop;


import static com.outstagram.outstagram.common.SessionConst.LOGIN_USER;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
public class LoginAspect {

    @Pointcut("execution(* *(.., @com.outstagram.outstagram.common.annotation.Login (*), ..))")
    public void loginRequired() {
    }


    @Around("loginRequired()")
    public Object checkSession(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("AOP - @Login Check Started");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
        HttpSession session = request.getSession(false);

        if (session == null) {
            log.info("AOP - @Login Check Result - session empty");
            throw new ApiException(ErrorCode.UNAUTHORIZED_USER);
        }

        UserDTO user = (UserDTO) session.getAttribute(LOGIN_USER);
        if (user == null) {
            log.info("AOP - @Login Check Result - user empty");
            throw new ApiException(ErrorCode.UNAUTHORIZED_USER);
        }

        /*
         joinPoint 안는 현재 실행중인 메서드에 대한 정보들을 담고 있음(메소드 이름, 타입, 파라미터)
         getArgs() : 현재 메서드에 전달된 파라미터들을 객체 배열 형태로 리턴
         이 파라미터들 중에서 타입이 UserDTO인 것을 찾아 현재 세션의 유저 정보를 넣어줄 것임
        */
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof UserDTO) {
                args[i] = user;
            }
        }

        /*
         @LoginSession UserDto user -> 이 user에 현재 session에 있는 유저를 넣어준다
         위에서 변경한 파라미터를 적용하려면 파라미터 배열을 proceed() 메서드에 전달해야 함
        */
        return joinPoint.proceed(args);

    }

}
