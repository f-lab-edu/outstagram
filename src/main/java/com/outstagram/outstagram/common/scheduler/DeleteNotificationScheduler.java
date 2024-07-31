package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.config.database.DataSourceContextHolder;
import com.outstagram.outstagram.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteNotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")    // 매일 오전 3시 정각에 실행
    public void deleteNotificationScheduler() {
        log.info("================== 30일 지난 알림 삭제 스케줄링 시작!!");
        // 30일 지난 알림 삭제하기
        for (long shardId = 0; shardId < DB_COUNT; shardId++) {
            log.info("=================== {}번 shard에서 30일 지난 알림 삭제 스케쥴링 시작", shardId);
            DataSourceContextHolder.setShardId(shardId);
            notificationService.deleteOlderThan30Days();
            DataSourceContextHolder.clearShardId();
        }

        log.info("================== 30일 지난 알림 삭제 스케줄링 종료!!");
    }
}
