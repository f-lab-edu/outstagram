package com.outstagram.outstagram.service;

import com.outstagram.outstagram.exception.ApiException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RedisMultiThreadTest {

    @Autowired
    private PostService postService;


    @Test
    @Transactional
    public void testConcurrentIncreaseLike() throws InterruptedException {
        Long postId = 11L;
        Long userId = 4L;

        // Initial setup to ensure post exists and is cached
        postService.loadLikeCountIfAbsent(postId);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            try {
                postService.increaseLike(postId, userId);
            } catch (ApiException e) {
                System.out.println(e.getErrorCode()); // Handle expected exception
            }
        };

        // Execute tasks concurrently
        executorService.execute(task);
        executorService.execute(task);

        // Shutdown executor and wait for tasks to finish
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Further assertions or cleanup can be done here
    }

    @Test
    @Transactional
    public void testConcurrentDecreaseLike() throws InterruptedException {
        Long postId = 14L;
        Long userId = 4L;

        // Initial setup to ensure post exists and is cached
        postService.loadLikeCountIfAbsent(postId);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            try {
                postService.unlikePost(postId, userId);
            } catch (ApiException e) {
                System.out.println(e.getErrorCode()); // Handle expected exception
            }
        };

        // Execute tasks concurrently
        executorService.execute(task);
        executorService.execute(task);

        // Shutdown executor and wait for tasks to finish
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Further assertions or cleanup can be done here
    }
}
