package com.outstagram.outstagram.util;

import static org.junit.jupiter.api.Assertions.*;

import com.outstagram.outstagram.config.SnowflakeConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class SnowflakeTest {

    @DisplayName("단일 스레드에서 id 1000개 생성하고 모두 고유한지 확인")
    @Test
    public void testSingleThreadedSnowflake() {
        Snowflake snowflake = Snowflake.getInstance(1);
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            long id = snowflake.nextId();
            System.out.println(snowflake.getNodeId() + "    " + snowflake.getSequence());
            System.out.println(Arrays.toString(snowflake.parse(id)));
            assertTrue(ids.add(id), "ID는 고유해야 합니다.");
        }
    }

    @DisplayName("10개 스레드가 각각 id 1000개씩 생성하고 모두 고유한지 확인")
    @Test
    public void testMultiThreadedSnowflake() throws ExecutionException, InterruptedException {
        final int threadCount = 10;
        final int idCount = 1000;

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            SnowflakeConfig.class);
        Snowflake snowflake = context.getBean(Snowflake.class); // 싱글톤 인스턴스 사용
        System.out.println(snowflake.getNodeId());
        System.out.println(snowflake.getSequence());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<Long> ids = new HashSet<>();
        Set<Future<Set<Long>>> futures = new HashSet<>();

        Callable<Set<Long>> task = () -> {
            Set<Long> localIds = new HashSet<>();
            for (int i = 0; i < idCount; i++) {
                long id = snowflake.nextId();
                localIds.add(id);
            }

            return localIds;
        };

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(task));
        }

        for (Future<Set<Long>> future : futures) {
            Set<Long> result = future.get();
//            System.out.println(result);
            ids.addAll(result);
        }

        executor.shutdown();

        // Check that all IDs are unique
        assertEquals(threadCount * idCount, ids.size(), "All generated IDs should be unique");
    }

    // 멀티 스레드가 되는 순간 테스트 실패
    @Test
    public void testDistributedSnowflake() throws Exception {
        final int nodeCount = 5;
        final int threadCount = 2;
        final int idCount = 1000;
        Set<Long> ids = new HashSet<>();
        Set<Future<Set<Long>>> futures = new HashSet<>();
        ExecutorService executor = Executors.newFixedThreadPool(nodeCount * threadCount);

        for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
            int finalNodeId = nodeId;
            Callable<Set<Long>> task = () -> {
                Snowflake snowflake = new Snowflake(finalNodeId); // 각 노드가 별도의 Snowflake 인스턴스를 사용하도록 수정
                Set<Long> localIds = new HashSet<>();
                for (int i = 0; i < idCount; i++) {
                    long id = snowflake.nextId();
                    localIds.add(id);
//                    Thread.sleep(2);
                }
                return localIds;
            };

            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(task));
            }
        }

        for (Future<Set<Long>> future : futures) {
            Set<Long> result = future.get();
            System.out.println(result.size());
            ids.addAll(result);
            System.out.println("after add: " + ids.size());
        }

        executor.shutdown();

        // Check that all IDs are unique
        assertEquals(nodeCount * threadCount * idCount, ids.size(), "All generated IDs should be unique");
    }

}