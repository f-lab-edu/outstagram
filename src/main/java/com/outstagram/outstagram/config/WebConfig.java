package com.outstagram.outstagram.config;

import com.outstagram.outstagram.common.interceptor.LoggingInterceptor;
import com.outstagram.outstagram.common.interceptor.LoginCheckInterceptor;
import com.outstagram.outstagram.common.interceptor.ShardInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Log, LoginCheck 인터셉터 적용
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor())
            .order(1)
            .addPathPatterns("/**");

        registry.addInterceptor(new LoginCheckInterceptor())
            .order(2)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/api/users/check-duplicated-email", "/api/users/check-duplicated-nickname",
                "/api/users/signup", "/api/users/login"
            );

        // 무조건 LoginCheckInterceptor 뒤에 있어야 함(그래야 shardId 설정 가능)
        registry.addInterceptor(new ShardInterceptor())
                .order(3)
                .addPathPatterns("/**");
    }

//    /**
//     * LoginMemberArgumentResolver 등록
//     */
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        resolvers.add(new LoginMemberArgumentResolver());
//    }

}
