package com.outstagram.outstagram.common.aop;


import com.outstagram.outstagram.common.annotation.LoadShardIdFromPostId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class LoadShardIdFromPostIdAspect {
    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(loadShardIdFromPostId) && args(postId, ..)")
    public Object queryAllShards(ProceedingJoinPoint joinPoint, LoadShardIdFromPostId loadShardIdFromPostId, Long postId) throws Throwable {
        log.info("============== @LoadShardIdFromPostId Started");
        Long currentShardId = (Long) RequestContextHolder.getRequestAttributes().getAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
        String shardId = redisTemplate.opsForValue().get(postId.toString()).toString();
        if (shardId != null) {
            RequestContextHolder.getRequestAttributes().setAttribute("shardId", Long.valueOf(shardId), RequestAttributes.SCOPE_REQUEST);
            log.info("============== Loaded shardId : {} for postId : {}", shardId, postId);
        }

        try {
            return joinPoint.proceed();
        } finally {
            // 기존 shardId(현재 로그인한 유저가 저장된 shardId)로 다시 전환
            RequestContextHolder.getRequestAttributes().setAttribute("shardId", currentShardId, RequestAttributes.SCOPE_REQUEST);
            log.info("============== Restored original shardId : {}", currentShardId);
        }
    }
}
