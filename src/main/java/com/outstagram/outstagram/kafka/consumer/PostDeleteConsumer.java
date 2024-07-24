package com.outstagram.outstagram.kafka.consumer;

import static com.outstagram.outstagram.common.constant.KafkaConst.DELETE_GROUPID;
import static com.outstagram.outstagram.common.constant.KafkaConst.DELETE_TOPIC;

import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.BookmarkMapper;
import com.outstagram.outstagram.mapper.CommentMapper;
import com.outstagram.outstagram.mapper.ImageMapper;
import com.outstagram.outstagram.mapper.LikeMapper;
import com.outstagram.outstagram.mapper.PostMapper;
import com.outstagram.outstagram.service.PostElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostDeleteConsumer {

    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final BookmarkMapper bookmarkMapper;
    private final CommentMapper commentMapper;
    private final ImageMapper imageMapper;

    private final PostElasticsearchService postElasticsearchService;

    @KafkaListener(topics = DELETE_TOPIC, groupId = DELETE_GROUPID, containerFactory = "postDeleteKafkaListenerContainerFactory")
    public void receive(@Payload Long postId) {
        log.info("=========== 게시물 관련 레코드 삭제 시작, postId = {}", postId);
        // post에서 soft delete
        int postResult = postMapper.deleteById(postId);
        if (postResult == 0) {
            throw new ApiException(ErrorCode.DELETE_ERROR, "게시물 삭제 오류 발생!!");
        }

        // like에서 hard delete
        int likeResult = likeMapper.deleteByPostId(postId);
        if (likeResult == 0) {
            log.info("====================== 좋아요 기록이 없습니다!!");
        }

        // bookmark에서 hard delete
        int bookmarkResult = bookmarkMapper.deleteByPostId(postId);
        if (bookmarkResult == 0) {
            log.info("====================== 북마크 기록이 없습니다!!");
        }

        // comment에서 soft delete
        int commentResult = commentMapper.deleteByPostId(postId);
        if (commentResult == 0) {
            log.info("====================== 댓글 기록이 없습니다!!");
        }

        // image에서 soft delete
        int imageResult = imageMapper.deleteByPostId(postId);
        if (imageResult == 0) {
            log.info("====================== 이미지 기록이 없습니다!!");
        }

        // elasticsearch DB에서도 삭제
        postElasticsearchService.deleteById(postId);

        log.info("=========== 게시물 관련 레코드 삭제 종료");
    }


}