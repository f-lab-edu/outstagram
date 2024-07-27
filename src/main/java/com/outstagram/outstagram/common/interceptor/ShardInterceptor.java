package com.outstagram.outstagram.common.interceptor;


import com.outstagram.outstagram.common.constant.DBConst;
import com.outstagram.outstagram.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;

@Slf4j
public class ShardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // 세션이 없으면 null 반환
        if (session != null) {
            UserDTO user = (UserDTO) session.getAttribute(LOGIN_USER);
            log.info("================ shard 세팅 인터셉터 실행 : user = {}", user);
            if (user != null) {
                Long shardId = user.getId() % DBConst.DB_COUNT;
                RequestContextHolder.getRequestAttributes().setAttribute("shardId", shardId, RequestAttributes.SCOPE_REQUEST);
                log.info("================ shard 세팅 완료 : {}", shardId);
            }
        } else {
            log.warn("Session does not exist. Skipping shard ID assignment.");
        }
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 요청이 완료된 후에 shardId를 제거
        RequestContextHolder.getRequestAttributes().removeAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
    }
}

