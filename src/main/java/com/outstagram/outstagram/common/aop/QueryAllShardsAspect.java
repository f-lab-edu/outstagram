package com.outstagram.outstagram.common.aop;


import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
public class QueryAllShardsAspect {
    private static final int SHARD_COUNT = (int) DB_COUNT;
    private final ExecutorService executor = Executors.newFixedThreadPool(SHARD_COUNT);

    @Pointcut("@annotation(com.outstagram.outstagram.common.annotation.QueryAllShards)")
    public void queryAll() {
    }

    @Around("queryAll()")
    public Object queryAllShards(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("============== @QueryAllShards Started");
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Object>[] futures = new CompletableFuture[SHARD_COUNT];

        for (int shardId = 0; shardId < SHARD_COUNT; shardId++) {
            final int currentShardId = shardId;
            futures[shardId] = CompletableFuture.supplyAsync(() -> {
                // 비동기 스레드에서 RequestAttributes를 설정합니다.
                RequestContextHolder.setRequestAttributes(requestAttributes);
                RequestContextHolder.getRequestAttributes().setAttribute("shardId", currentShardId,
                    ServletRequestAttributes.SCOPE_REQUEST);
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return null;
                } finally {
                    RequestContextHolder.getRequestAttributes().removeAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
                }
            }, executor);
        }

        for (int shardId = 0; shardId < SHARD_COUNT; shardId++) {
            Object result = futures[shardId].get();
            if (result != null) {
                RequestContextHolder.getRequestAttributes().setAttribute("shardId", shardId, ServletRequestAttributes.SCOPE_REQUEST);
                return result;
            }
        }
        return null;
    }
}
