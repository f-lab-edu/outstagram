package com.outstagram.outstagram.async;


import com.outstagram.outstagram.dto.PostDetailsDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPostService {
    private final PostService postService;

    @Async
    public CompletableFuture<PostDetailsDTO> findPostDetailsFromDB(Long postId, Long userId, Long shardId) {
        RequestContextHolder.getRequestAttributes().setAttribute("shardId", shardId, RequestAttributes.SCOPE_REQUEST);
        PostDetailsDTO postDetails = postService.getPostDetails(postId, userId);
        RequestContextHolder.getRequestAttributes().removeAttribute("shardId", RequestAttributes.SCOPE_REQUEST);

        return CompletableFuture.completedFuture(postDetails);
    }

    public PostDetailsDTO getPostDetails(Long postId, Long userId) throws ExecutionException, InterruptedException {
        List<CompletableFuture<PostDetailsDTO>> futures = new ArrayList<>();

        for (long i = 0; i < DB_COUNT; i++) {
            futures.add(findPostDetailsFromDB(postId, userId, i));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();  // 모든 비동기 작업이 완료될 때까지 대기

        List<PostDetailsDTO> results = new ArrayList<>();
        for (CompletableFuture<PostDetailsDTO> future : futures) {
            PostDetailsDTO postDetails = future.get();  // 각각의 결과를 얻음
            if (postDetails != null) {
                results.add(postDetails);  // 결과를 리스트에 추가
            }
        }

        if (results.isEmpty())
            throw new ApiException(ErrorCode.POST_NOT_FOUND);  // 결과가 없는 경우 예외 발생

        return results.get(0);
    }
}