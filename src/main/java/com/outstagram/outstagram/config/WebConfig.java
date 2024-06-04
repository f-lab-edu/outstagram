package com.outstagram.outstagram.config;

import com.outstagram.outstagram.common.filter.LoggingInterceptor;
import com.outstagram.outstagram.common.filter.LoginCheckInterceptor;
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
    }

//    /**
//     * LoginMemberArgumentResolver 등록
//     */
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        resolvers.add(new LoginMemberArgumentResolver());
//    }

}
