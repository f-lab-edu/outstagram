package com.outstagram.outstagram.common.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    public static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler)
        throws Exception {
        String requestURI = request.getRequestURI();

        String uuid = UUID.randomUUID().toString(); // HTTP 요청 구분하기 위한 uuid
        request.setAttribute(LOG_ID,
            uuid); // postHandle, afterCompletion 에서도 해당 요청에 대한 uuid 값을 사용하기 위해서 저장해놓기

        log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse
        response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String) request.getAttribute(LOG_ID);

        log.info("RESPONSE [{}][{}]", logId, requestURI);
        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }
    }
}
