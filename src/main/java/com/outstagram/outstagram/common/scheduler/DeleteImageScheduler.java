package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteImageScheduler {

    private final ImageService imageService;

//    @Scheduled(fixedRate = 10000)
//    @Scheduled(cron = "0 35 19 * * *")
    @Scheduled(cron = "0 0 2 * * *")    // 매일 오전 2시 정각에 실행
    public void deleteImageScheduler() {
        log.info("================== 이미지 삭제 스케쥴링 시작!!");
        // image table에서 is_deleted = 1인 데이터 찾기
        List<ImageDTO> deletedImages = imageService.getDeletedImages();
        if (deletedImages == null) {
            throw new ApiException(ErrorCode.NULL_POINT_ERROR, "삭제할 이미지 조회에 대한 NPE!!");
        }
        if (deletedImages.isEmpty()) {
            log.info("================== 삭제할 이미지가 없습니다!!");
            log.info("================== 이미지 삭제 스케쥴링 종료!!");
            return;
        }
        // 해당 데이터의 이미지 경로를 통해서 이미지 삭제하기
        imageService.deleteLocalImages(deletedImages);
        log.info("============================ 로컬 이미지 삭제 완료!");
        imageService.hardDeleteByIds(deletedImages);
        log.info("============================ image 테이블에서 해당 레코드들 삭제 완료!");

        log.info("================== 이미지 삭제 스케쥴링 종료!!");
    }
}
