package com.outstagram.outstagram.common.aop;


import com.outstagram.outstagram.common.annotation.CacheShardId;
import com.outstagram.outstagram.dto.PostDTO;
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
public class CacheShardIdAspect {
    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(cacheShardId)")
    public Object queryAllShards(ProceedingJoinPoint joinPoint, CacheShardId cacheShardId) throws Throwable {
        log.info("============== @CacheShardId Started");
        Object result = joinPoint.proceed();
        Long postId = ((PostDTO) result).getId();
        Long shardId = (Long) RequestContextHolder.getRequestAttributes().getAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
        redisTemplate.opsForValue().set(postId.toString(), shardId);
        log.info("============== Cached shardId: {} for postId : {}", shardId, postId);
        return result;
    }
}
