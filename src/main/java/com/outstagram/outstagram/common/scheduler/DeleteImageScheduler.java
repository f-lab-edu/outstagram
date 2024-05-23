package com.outstagram.outstagram.common.scheduler;

import com.outstagram.outstagram.dto.ImageDTO;
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

    @Scheduled(fixedRate = 10000)
    public void deleteImageScheduler() {
        log.info("================== 이미지 삭제 스케쥴링 시작!!");
        // image table에서 is_deleted = 1인 데이터 찾기
        List<ImageDTO> deletedImages = imageService.getDeletedImages();

        if (deletedImages.isEmpty()) {
            log.info("================== 삭제할 이미지가 없습니다!!!");
        }
        // 해당 데이터의 이미지 경로를 통해서 이미지 삭제하기
        imageService.hardDeleteImages(deletedImages);
        log.info("================== 이미지 삭제 스케쥴링 종료!!");
    }
}
