package com.outstagram.outstagram.common.aop;


import com.outstagram.outstagram.common.annotation.LoadShardIdFromPostId;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class LoadShardIdFromPostIdAspect {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;

    @Around("@annotation(loadShardIdFromPostId) && args(postId, ..)")
    public Object queryAllShards(ProceedingJoinPoint joinPoint, LoadShardIdFromPostId loadShardIdFromPostId, Long postId) throws Throwable {
        log.info("============== @LoadShardIdFromPostId Started");
        Long currentShardId = (Long) RequestContextHolder.getRequestAttributes().getAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
        Object shardId = redisTemplate.opsForValue().get(postId.toString());
        if (shardId != null) {
            RequestContextHolder.getRequestAttributes().setAttribute("shardId", Long.valueOf(shardId.toString()), RequestAttributes.SCOPE_REQUEST);
            log.info("============== Loaded shardId : {} for postId : {}", shardId, postId);
        } else {
            // 모든 db에 조회해서 shardId 찾아내기
            log.info("============== ShardId not found in cache, querying all shards");
            Long shard = queryAllShardsForPostId(postId);
            if (shard != null) {
                redisTemplate.opsForValue().set(postId.toString(), shard);
                RequestContextHolder.getRequestAttributes().setAttribute("shardId", shard, RequestAttributes.SCOPE_REQUEST);
                log.info("============== Found and cached shardId : {} for postId : {}", shard, postId);
            } else {
                log.info("============== PostId not found in DB and  in cache!!! postId : {}", postId);
                throw new ApiException(ErrorCode.POST_NOT_FOUND);
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            // 기존 shardId(현재 로그인한 유저가 저장된 shardId)로 다시 전환
            RequestContextHolder.getRequestAttributes().setAttribute("shardId", currentShardId, RequestAttributes.SCOPE_REQUEST);
            log.info("============== Restored original shardId : {}", currentShardId);
        }
    }

    private Long queryAllShardsForPostId(Long postId) throws Exception {
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (long shardId = 0; shardId < DB_COUNT; shardId++) {
            futures.add(findById(postId,  shardId));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();

        for (CompletableFuture<Long> future : futures) {
            Long shardId = future.get();
            if (shardId != -1) {
                return shardId;
            }
        }

        return null;
    }

    @Async
    protected CompletableFuture<Long> findById(Long postId, Long shardId) {
        RequestContextHolder.getRequestAttributes().setAttribute("shardId", shardId, RequestAttributes.SCOPE_REQUEST);
        PostDTO post = postMapper.findById(postId);
        RequestContextHolder.getRequestAttributes().removeAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
        return CompletableFuture.completedFuture(post != null ? shardId : -1);
    }
}
