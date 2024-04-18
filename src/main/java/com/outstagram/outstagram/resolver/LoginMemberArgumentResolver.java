package com.outstagram.outstagram.resolver;


import static com.outstagram.outstagram.common.session.SessionConst.LOGIN_USER;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * @Login 애노테이션 붙어있으면서 UserDTO 타입이면 해당 ArgumentResolver 사용
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("supportsParameter 실행");
        boolean hasLoginAnnotation =
            parameter.hasParameterAnnotation(Login.class);
        boolean hasUserType =
            UserDTO.class.isAssignableFrom(parameter.getParameterType());
        return hasLoginAnnotation && hasUserType;
    }


    /**
     * 컨트롤러 호출되기 직전에 호출되서 세션에서 유저 정보 찾아서 반환해준다. 없으면 null 리턴
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
        ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) throws Exception {
        log.info("resolveArgument 실행");
        HttpServletRequest request = (HttpServletRequest)
            webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute(LOGIN_USER);
    }
}