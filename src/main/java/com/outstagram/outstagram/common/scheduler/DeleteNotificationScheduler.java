package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteNotificationScheduler {

    private final NotificationService notificationService;

//    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 38 11 * * *")
    @Transactional
//    @Scheduled(cron = "0 0 3 * * *")    // 매일 오전 3시 정각에 실행
    public void deleteNotificationScheduler() {
        log.info("================== 30일 지난 알림 삭제 스케줄링 시작!!");
        // 30일 지난 알림 삭제하기
        notificationService.deleteOlderThan30Days();

        log.info("================== 30일 지난 알림 삭제 스케줄링 종료!!");
    }
}