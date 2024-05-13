package com.outstagram.outstagram;

import com.outstagram.outstagram.producer.FeedUpdateProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class KafkaConsumerTest {

    @Autowired
    private FeedUpdateProducer producer;

    @Test
    public void testKafka() throws InterruptedException {
        String topic = "feed";
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long postId = Integer.toUnsignedLong(i);
            executorService.submit(() -> {
                try {
                    producer.send(topic, postId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

    }
}
