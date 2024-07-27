package com.outstagram.outstagram.service;


import static com.outstagram.outstagram.common.constant.CacheConst.IN_CACHE;
import static com.outstagram.outstagram.common.constant.CacheConst.NOT_FOUND;
import static com.outstagram.outstagram.common.constant.CacheConst.POST;
import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;
import static com.outstagram.outstagram.common.constant.KafkaConst.POST_UPSERT_TOPIC;
import static com.outstagram.outstagram.common.constant.KafkaConst.SEND_NOTIFICATION;
import static com.outstagram.outstagram.common.constant.PageConst.PAGE_SIZE;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.FEED;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.LIKE_COUNT_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_BOOKMARK_PREFIX;
import static com.outstagram.outstagram.common.constant.RedisKeyPrefixConst.USER_LIKE_PREFIX;
import static com.outstagram.outstagram.dto.AlarmType.COMMENT;
import static com.outstagram.outstagram.dto.AlarmType.LIKE;
import static com.outstagram.outstagram.dto.AlarmType.REPLY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.outstagram.outstagram.controller.request.CreateCommentReq;
import com.outstagram.outstagram.controller.request.CreatePostReq;
import com.outstagram.outstagram.controller.request.EditCommentReq;
import com.outstagram.outstagram.controller.request.EditPostReq;
import com.outstagram.outstagram.dto.BookmarkRecordDTO;
import com.outstagram.outstagram.dto.CommentDTO;
import com.outstagram.outstagram.dto.CommentUserDTO;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.dto.LikeCountDTO;
import com.outstagram.outstagram.dto.LikeRecordDTO;
import com.outstagram.outstagram.dto.PostDTO;
import com.outstagram.outstagram.dto.PostDetailsDTO;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.kafka.producer.FeedUpdateProducer;
import com.outstagram.outstagram.kafka.producer.NotificationProducer;
import com.outstagram.outstagram.kafka.producer.PostDeleteProducer;
import com.outstagram.outstagram.kafka.producer.PostProducer;
import com.outstagram.outstagram.mapper.PostMapper;
import com.outstagram.outstagram.util.Snowflake;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final Snowflake snowflake;
    private final PostMapper postMapper;
    private final ImageService imageService;
    private final UserService userService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final CommentService commentService;
    private final FeedUpdateProducer feedUpdateProducer;
    private final PostDeleteProducer postDeleteProducer;
    private final NotificationProducer notificationProducer;
    private final PostProducer postProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SqlSessionFactory sqlSessionFactory;
    private final DefaultRedisScript<String> increaseLikeScript;
    private final DefaultRedisScript<String> decreaseLikeScript;
    private final ObjectMapper objectMapper;

    /**
     * 게시물 존재 여부 검증
     */
    private static PostDTO validatePostExist(Long postId) {
        // 존재하는 post인지 검증
        PostService proxy = (PostService) AopContext.currentProxy();
        PostDTO post = proxy.getPost(postId);

        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    @Transactional
    public void insertPost(CreatePostReq createPostReq, Long userId) {
        long postId = snowflake.nextId(userId % DB_COUNT);

        PostDTO newPost = PostDTO.builder()
                .id(postId)
                .contents(createPostReq.getContents())
                .userId(userId)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // 게시물 내용 저장 (insertPost 정상 실행되면, newPost의 id 속성에 id값이 들어 있다)
        postMapper.insertPost(newPost);

        // 로컬 디렉토리에 이미지 저장 후, DB에 이미지 정보 저장
        imageService.saveImages(createPostReq.getImgFiles(), postId);

        // kafka에 메시지 발행 : 팔로워들의 피드목록에 내가 작성한 게시물 ID 넣기
        feedUpdateProducer.send("feed", userId, postId);

        // ES DB에도 저장
        postProducer.save(POST_UPSERT_TOPIC, newPost);
    }

    public List<PostDetailsDTO> getMyPosts(Long userId, Long lastId) {
        // 유저가 작성한 최신 게시물 11개씩 가져오기 (11개 가져와지면 다음 페이지 존재하는 것)
        List<Long> ids = postMapper.findIdsByUserId(userId, lastId, PAGE_SIZE + 1);

        PostService proxy = (PostService) AopContext.currentProxy();

        return ids.stream()
                .map(id -> proxy.getPostDetails(id, userId))
                .collect(Collectors.toList());
    }

    public List<PostDTO> findByKeyword(String keyword) {
        return postMapper.findByKeyword(keyword);
    }

    /**
     * 순수 게시물 캐싱
     */
    @Cacheable(value = POST, key = "#postId")
    public PostDTO getPost(Long postId) {
        return postMapper.findById(postId);
    }

    /**
     * 각 캐싱된 순수 게시물 + 이미지 정보 + 댓글 + 좋아요 + 북마크 들을 조합해서 종합 게시물 만들어주는 메서드
     */
    public PostDetailsDTO getPostDetails(Long postId, Long userId) {
        // 내부 메서드 호출할 때, @Cacheable 적용되도록 하려면 프록시 객체를 통해서 메서드를 호출해야 함.
        // 존재하는 post인지 검증
        PostService proxy = (PostService) AopContext.currentProxy();
        PostDTO post = proxy.getPost(postId);
        if (post == null) return null;


        // Post의 이미지 정보 가져오기(캐시 사용)
        List<ImageDTO> imageList = imageService.getImageInfos(post.getId());

        // 로그인한 유저가 게시물 작성자인지 판단
        boolean isAuthor = post.getUserId().equals(userId);

        // 작성자 정보 가져오기 -> nickname과 유저 img 가져오기 위해서(캐시 사용)
        UserDTO author = userService.getUser(post.getUserId());

        // 이미지 url 조합하기
        Map<Long, String> imageUrlMap = new HashMap<>();
        for (ImageDTO img : imageList) {
            imageUrlMap.put(img.getId(), img.getImgUrl());
        }

        // Redis에서 게시물의 좋아요 개수 가져오기(캐시 사용)
        int likeCount = loadLikeCountIfAbsent(postId);

//        // 현재 유저가 해당 게시물 좋아요 눌렀는지(1:DB에 있음, 2:cache에 있음)
//        boolean existLike = likeService.existsLike(userId, post.getId()) > 0;
//
//        // 현재 유저가 해당 게시물 북마크 했는지
//        boolean existBookmark = bookmarkService.existsBookmark(userId, post.getId()) > 0;

        // 게시물의 모든 댓글들(캐시 사용)
        List<CommentUserDTO> comments = commentService.getComments(post.getId());

        // 캐싱 데이터 조합해 종합 게시물 만들기
        return PostDetailsDTO.builder()
                .postId(postId)
                .userId(author.getId())
                .nickname(author.getNickname())
                .userImgUrl(author.getImgUrl())
                .contents(post.getContents())
                .postImgUrls(imageUrlMap)
                .likes(likeCount)
                .isCreatedByCurrentUser(isAuthor)
                .comments(comments)
                .build();
    }

    /**
     * 피드 가져오기
     */
    public List<PostDetailsDTO> getFeed(Long lastId, Long userId) {

        String userFeedKey = FEED + userId;

        // 캐시에서 피드 목록 가져오기
        List<Long> feedList = redisTemplate.opsForList()
                .range(userFeedKey, 0, -1)
                .stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();

        int feedIdSize = feedList.size();

        List<Long> idList = new ArrayList<>();

        if (lastId == null) {
            if (feedIdSize > PAGE_SIZE) {
                idList.addAll(feedList.subList(0, PAGE_SIZE + 1));
                log.info("========= 캐시에서 가져온 id List : {}", idList);
            } else {
                idList.addAll(feedList);
                log.info("========= 캐시에서 가져온 id List : {}", idList);

                int need = PAGE_SIZE + 1 - idList.size();
                List<Long> feedListFromDB = getFeedIdsFromDB(userId, idList.get(idList.size() - 1), need);
                log.info("========= DB에서 가져온 id List : {}", feedListFromDB);
                idList.addAll(feedListFromDB);
            }
        } else {
            int startIdx = 0;
            for (int i = 0; i < feedIdSize; i++) {
                if (feedList.get(i).equals(lastId)) {
                    startIdx = i + 1;
                    break;
                }
            }

            // lastId가 feed 목록에 없다 == DB에서 조회해야 함
            if (startIdx == 0 || startIdx == feedIdSize) {
                List<Long> feedListFromDB = getFeedIdsFromDB(userId, lastId, PAGE_SIZE + 1);
                log.info("========= DB에서 가져온 id List : {}", feedListFromDB);
                idList.addAll(feedListFromDB);
            } else {
                int endIdx = Math.min((startIdx + PAGE_SIZE + 1), feedIdSize);
                idList.addAll(feedList.subList(startIdx, endIdx));
                log.info("========= 캐시에서 가져온 id List : {}", idList);
                int need = PAGE_SIZE + 1 - idList.size();

                if (need != 0) {
                    List<Long> feedListFromDB = getFeedIdsFromDB(userId, idList.get(idList.size() - 1), need);
                    log.info("========= DB에서 가져온 id List : {}", feedListFromDB);
                    idList.addAll(feedListFromDB);
                }
            }

        }

        PostService proxy = (PostService) AopContext.currentProxy();
        List<PostDetailsDTO> postDetailList = new ArrayList<>(11);
        idList.forEach(id -> postDetailList.add(proxy.getPostDetails(id, userId)));

        return postDetailList;
    }

    private List<Long> getFeedIdsFromDB(Long userId, Long lastId, int size) {
        return postMapper.getFeedIdsFromDB(userId, lastId, size);
    }

    @Transactional(rollbackFor = Exception.class)   // 해당 transaction 안에서 Exception 발생하면 무조건 rollback 실행
    @Caching(evict = @CacheEvict(value = POST, key = "#postId"))    // editPost() 수행 후 캐시에서 삭제
    public void editPost(Long postId, EditPostReq editPostReq, Long userId) {
        // 수정할 게시물 가져오기
        PostService proxy = (PostService) AopContext.currentProxy();
        PostDTO post = proxy.getPost(postId);

        // 게시물 작성자인지 검증
        validatePostOwner(post, userId);

        // 삭제할 이미지가 있다면 삭제하기(soft delete)
        if (editPostReq.getDeleteImgIds() != null && !editPostReq.getDeleteImgIds().isEmpty()) {
            imageService.softDeleteByIds(postId, editPostReq.getDeleteImgIds());    // 이미지 정보 캐시 삭제
        }

        // 추가할 이미지가 있다면 추가하기
        if (editPostReq.getImgFiles() != null && !editPostReq.getImgFiles().isEmpty()) {
            imageService.saveImages(editPostReq.getImgFiles(), post.getId());
        }

        // 수정할 내용이 있다면 수정하기
        if (!Objects.equals(editPostReq.getContents(), post.getContents())) {
            int result = postMapper.updateContentsById(postId, editPostReq.getContents());
            if (result == 0) {
                throw new ApiException(ErrorCode.UPDATE_ERROR, "게시물 내용 수정 오류!!");
            }
            PostDTO updatePost = postMapper.findById(postId);
            postProducer.edit(POST_UPSERT_TOPIC, updatePost);
        }

    }

    /* ========================================================================================== */

    /**
     * 게시물 삭제 비동기 처리
     */
    @Transactional
    @Caching(evict = @CacheEvict(value = POST, key = "#postId"))
    public void deletePost(Long postId, Long userId) {

        // 게시물이 존재하는지 & 삭제 권한 있는지 검증
        validatePostOwner(postId, userId);

        // 게시물 삭제 비동기 처리
        postDeleteProducer.send("post-delete", postId);
    }

    /**
     * postId에 대한 좋아요 개수 캐싱되어 있는지 확인 캐싱 안되어 있으면 DB에서 postId의 좋아요 개수를 Redis로 캐싱
     */
    public Integer loadLikeCountIfAbsent(Long postId) {
        String key = LIKE_COUNT_PREFIX + postId;
        int likeCount;
        // Redis에 좋아요 개수 캐싱된적 없으면 -> DB에서 가져와서 좋아요 개수 캐싱하기
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            // 삭제되거나 없는 게시물이면 예외 던지기
            PostDTO post = postMapper.findById(postId);
            if (post == null) {
                throw new ApiException(ErrorCode.POST_NOT_FOUND);
            }
            likeCount = post.getLikes();
            redisTemplate.opsForValue().set(key, likeCount);
            redisTemplate.expire(key, Duration.ofHours(1));
        } else {
            likeCount = (int) redisTemplate.opsForValue().get(key);
        }

        return likeCount;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void increaseLike(Long postId, Long userId) {
        // Redis에 게시물에 대한 좋아요 개수 캐싱하기(없으면 DB에서 좋아요 개수 가져와서 캐싱하고 있으면 pass)
        loadLikeCountIfAbsent(postId); // 게시물 존재 여부도 검증함

        String likeCountKey = LIKE_COUNT_PREFIX + postId;
        String userLikeKey = USER_LIKE_PREFIX + userId;

        // 캐시(2) or DB(1)에 좋아요 기록 있음 -> 중복 좋아요 방지
        if (likeService.existsLike(userId, postId) != NOT_FOUND) {
            throw new ApiException(ErrorCode.DUPLICATED_LIKE);
        }

        LikeRecordDTO likeRecord = new LikeRecordDTO(postId, LocalDateTime.now());

        // likeRecord 직렬화
        String likeRecordString;
        try {
            likeRecordString = objectMapper.writeValueAsString(likeRecord);
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorCode.JSON_CONVERTING_ERROR, "JSON 직렬화 오류");
        }

        RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();

        // lua script execute
        try {
            redisTemplate.execute(increaseLikeScript, stringSerializer, stringSerializer,
                    Arrays.asList(likeCountKey, userLikeKey), likeRecordString);
        } catch (RedisSystemException e) {
            throw new ApiException(e, ErrorCode.DUPLICATED_LIKE);
        }

        // 알림 보내기
        notificationProducer.send(SEND_NOTIFICATION, userId, postId, LIKE);
    }

    /**
     * 좋아요 취소 기능 - 게시물 좋아요 개수 1 감소 - like table에서 해당 기록 삭제
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void unlikePost(Long postId, Long userId) {
        loadLikeCountIfAbsent(postId);  // 게시물 존재 여부도 검증함

        String key = LIKE_COUNT_PREFIX + postId;
        String userLikeKey = USER_LIKE_PREFIX + userId;

        // 0 : 좋아요 기록 없음 | 1 : DB에 좋아요 기록 있음 | 2 : 캐시에 좋아요 기록 있음
        int existLike = likeService.existsLike(userId, postId);

        // 좋아요 존재하지 않는 경우 -> 취소 불가능
        if (existLike == NOT_FOUND) {
            throw new ApiException(ErrorCode.NOT_FOUND_LIKE);
        }

        // 캐시에 좋아요 기록 있는 경우
        if (existLike == IN_CACHE) {
            RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
            try {
                redisTemplate.execute(decreaseLikeScript, stringSerializer, stringSerializer,
                        Arrays.asList(key, userLikeKey), postId.toString());
            } catch (RedisSystemException e) {
                throw new ApiException(e, ErrorCode.NOT_FOUND_LIKE);
            }
        } else {    // DB에 좋아요 기록 있는 경우
            // DB에서 바로 delete
            likeService.deleteLike(userId, postId);
            redisTemplate.opsForValue().decrement(key, 1);
        }
    }

    public List<PostDetailsDTO> getLikePostsPlusOne(Long userId, Long lastId) {
        // 먼저 캐시 좋아요 누른 기록 확인 후 -> 모자라면 DB에서 좋아요 ID 목록 가져오기 (캐시에 항상 최신 데이터)
        String userLikeKey = USER_LIKE_PREFIX + userId;

        // 캐시에서 좋아요 누른 기록 가져오기
        List<LikeRecordDTO> recentLikeIdList = redisTemplate.opsForList()
                .range(userLikeKey, 0, -1)
                .stream()
                .map(record -> (LikeRecordDTO) record)
                .toList();

        int recentIdSize = recentLikeIdList.size();

        List<Long> idList = new ArrayList<>();

        if (lastId == null) {   // 첫 요청 : 캐시 -> DB
            if (recentIdSize > PAGE_SIZE) {      // 짜른게 11개 이상-> 캐시만으로 해결
                recentLikeIdList.stream()
                        .limit(PAGE_SIZE + 1)
                        .forEach(record -> idList.add(record.getPostId()));
            } else {    // 11개 미만 -> 캐시 + DB로 해결
                // 캐시에 있는 거 다 가져와 넣기
                recentLikeIdList.forEach(record -> idList.add(record.getPostId()));
                // 남은 개수 개산
                int need = PAGE_SIZE + 1 - idList.size();
                // 남은 개수만큼 DB에서 가져오기
                List<Long> likePostIds = likeService.getLikePostIds(userId, null, need);
                // DB에서 가져온거 넣기
                idList.addAll(likePostIds);
            }
        } else {    // 첫 요청을 제외한 모든 요청
            // lastId가 캐시에 있는지 확인
            int startIndex = 0;
            for (int i = 0; i < recentIdSize; i++) {
                if (recentLikeIdList.get(i).getPostId().equals(lastId)) {
                    startIndex = i + 1;  // lastId의 다음 인덱스부터 시작
                    break;
                }
            }

            // startIndex == 0 -> 캐시에 lastId가 없다
            // startIndex >= recentIdSize -> 캐시의 가장 마지막에 lastId가 있는 경우 = 캐시 다 읽어서 DB 읽자
            if (startIndex == 0 || startIndex == recentIdSize) {
                List<Long> likePostIds = likeService.getLikePostIds(userId, lastId, PAGE_SIZE + 1);
                idList.addAll(likePostIds);
            } else {    // lastId가 캐시에 있다 -> 캐시에서 페이징하고 남은만큼 DB에서 페이징
                int toIndex = Math.min((startIndex + PAGE_SIZE + 1), recentIdSize);
                recentLikeIdList.subList(startIndex, toIndex)
                        .forEach(record -> idList.add(record.getPostId()));

                int need = PAGE_SIZE + 1 - idList.size();
                if (need != 0) {    // 남은 건 DB에서 가져오기
                    List<Long> likePostIds = likeService.getLikePostIds(userId, null, need);
                    idList.addAll(likePostIds);
                }
            }
        }

        PostService proxy = (PostService) AopContext.currentProxy();
        List<PostDetailsDTO> postDetailList = new ArrayList<>(11);
        log.info("캐시와 DB 합친 좋아요 리스트 : {}", idList);

        idList.forEach(id -> postDetailList.add(proxy.getPostDetails(id, userId)));

        return postDetailList;
    }

    /* ========================================================================================== */

    /**
     * update 쿼리를 배치로 실행해 DB 왕복 줄이기
     */
    @Transactional
    public void updateLikeCountAll(List<LikeCountDTO> likeCountList) {
        if (likeCountList != null && !likeCountList.isEmpty()) {
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                PostMapper mapper = session.getMapper(PostMapper.class);
                likeCountList.forEach(
                        dto -> mapper.updateLikeCount(dto.getPostId(), dto.getLikeCount()));
                session.commit();
            }
        }
    }

    /**
     * 북마크 저장 메서드
     */
    @Transactional
    public void addBookmark(Long postId, Long userId) {
        validatePostExist(postId);

        String userBookmarkKey = USER_BOOKMARK_PREFIX + userId;

        // 캐시나 DB에 북마크 기록 있음 -> 중복 북마크 방지
        if (bookmarkService.existsBookmark(userId, postId) > 0) {
            throw new ApiException(ErrorCode.DUPLICATED_BOOKMARK);
        }

        BookmarkRecordDTO bookmarkRecord = new BookmarkRecordDTO(postId, LocalDateTime.now());
        redisTemplate.opsForList().leftPush(userBookmarkKey, bookmarkRecord);
    }

    /**
     * 북마크 취소 메서드
     */
    @Transactional
    public void deleteBookmark(Long postId, Long userId) {
        validatePostExist(postId);
        String userBookmarkKey = USER_BOOKMARK_PREFIX + userId;

        int existBookmark = bookmarkService.existsBookmark(userId, postId);

        // 북마크 존재하지 않는 경우 -> 취소 불가능
        if (existBookmark == NOT_FOUND) {
            throw new ApiException(ErrorCode.NOT_FOUND_BOOKMARK);
        }

        // 캐시에 북마크 기록 있는 경우
        if (existBookmark == IN_CACHE) {
            // 캐시에서 북마크 누른 기록 삭제
            redisTemplate.opsForList()
                    .range(userBookmarkKey, 0, -1)
                    .stream()
                    .map(record -> (BookmarkRecordDTO) record)
                    .filter(record -> record.getPostId().equals(postId))
                    .findFirst()
                    .ifPresent(
                            recordToRemove -> redisTemplate.opsForList()
                                    .remove(userBookmarkKey, 1, recordToRemove)
                    );
        } else {    // DB에 북마크 기록 있는 경우
            // DB에서 바로 delete
            bookmarkService.deleteBookmark(userId, postId);
        }
    }

    /* ========================================================================================== */

    /**
     * 로그인한 유저가 북마크한 모든 게시물 가져오기
     */
    public List<PostDetailsDTO> getBookmarkedPostsPlusOne(Long userId, Long lastId) {
        // 먼저 캐시 북마크 누른 기록 확인 후 -> 모자라면 DB에서 북마크 ID 목록 가져오기 (캐시에 항상 최신 데이터)
        String userBookmarkKey = USER_BOOKMARK_PREFIX + userId;

        // 캐시에서 북마크 누른 기록 가져오기
        List<BookmarkRecordDTO> recentBookmarkIdList = redisTemplate.opsForList()
                .range(userBookmarkKey, 0, -1)
                .stream()
                .map(record -> (BookmarkRecordDTO) record)
                .toList();

        int recentIdSize = recentBookmarkIdList.size();

        List<Long> idList = new ArrayList<>();

        if (lastId == null) {   // 첫 요청 : 캐시 -> DB
            if (recentIdSize > 10) {      // 짜른게 10개 초과 -> 캐시만으로 해결
                recentBookmarkIdList.stream()
                        .limit(PAGE_SIZE + 1)
                        .forEach(record -> idList.add(record.getPostId()));
            } else {
                recentBookmarkIdList.forEach(record -> idList.add(record.getPostId()));
                int need = PAGE_SIZE + 1 - idList.size();
                List<Long> bookmarkIds = bookmarkService.getBookmarkedPostIds(userId, null, need);
                idList.addAll(bookmarkIds);
            }
        } else {    // 첫 요청을 제외한 모든 요청
            // lastId가 캐시에 있는지 확인
            int startIndex = 0;
            for (int i = 0; i < recentIdSize; i++) {
                if (recentBookmarkIdList.get(i).getPostId().equals(lastId)) {
                    startIndex = i + 1;  // lastId의 다음 인덱스부터 시작
                    break;
                }
            }

            // lastId가 캐시에 없다 = 캐시는 이미 다 읽었거나, 없기 때문에  DB에서 가져와야 함
            if (startIndex == 0 || startIndex == recentIdSize) {
                List<Long> bookmarkIds = bookmarkService.getBookmarkedPostIds(userId, lastId,
                        PAGE_SIZE + 1);
                idList.addAll(bookmarkIds);
            } else {    // lastId가 캐시에 있다 -> 캐시에서 페이징하고 남은만큼 DB에서 페이징
                int toIndex = Math.min((startIndex + PAGE_SIZE + 1), recentIdSize);
                recentBookmarkIdList.subList(startIndex, toIndex)
                        .forEach(record -> idList.add(record.getPostId()));

                int need = PAGE_SIZE + 1 - idList.size();
                if (need != 0) {    // 남은 건 DB에서 가져오기
                    List<Long> bookmarkIds = bookmarkService.getBookmarkedPostIds(userId, null,
                            need);
                    idList.addAll(bookmarkIds);
                }
            }
        }

        PostService proxy = (PostService) AopContext.currentProxy();
        List<PostDetailsDTO> postDetailList = new ArrayList<>(11);
        log.info("캐시와 DB 합친 북마크 리스트 : {}", idList);

        idList.forEach(id -> postDetailList.add(proxy.getPostDetails(id, userId)));

        return postDetailList;
    }

    /**
     * 댓글 저장하는 로직
     */
    public void addComment(CreateCommentReq commentReq, Long postId, Long userId) {
        validatePostExist(postId);

        // 댓글 객체 생성하기
        CommentDTO newComment = CommentDTO.builder()
                .userId(userId)
                .postId(postId)
                .parentCommentId(null)
                .contents(commentReq.getContents())
                .level(false)
                .isDeleted(false)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // comment 테이블에 댓글 저장하기
        commentService.insertComment(newComment);

        // 알림 보내기
        notificationProducer.send(SEND_NOTIFICATION, userId, postId, COMMENT);
    }

    /**
     * 대댓글 저장하는 로직
     */
    public void addComment(CreateCommentReq commentReq, Long postId, Long commentId, Long userId) {
        // 존재하는 post인지 검증
        validatePostExist(postId);
        CommentDTO comment = commentService.findByIdAndPostId(postId, commentId);
        if (comment == null) {
            throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 대댓글 객체 생성하기
        CommentDTO newComment = CommentDTO.builder()
                .userId(userId)
                .postId(postId)
                .parentCommentId(commentId)
                .contents(commentReq.getContents())
                .level(true)
                .isDeleted(false)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // comment 테이블에 댓글 저장하기
        commentService.insertComment(newComment);

        // 알림 보내기
        notificationProducer.send(SEND_NOTIFICATION, userId, commentId, REPLY);
    }

    /**
     * (대)댓글 수정
     */
    public void editComment(EditCommentReq editCommentReq, Long postId, Long commentId,
                            Long userId) {
        // 게시물&댓글 존재 여부 검증, 작성자인지 검증하기
        validatePostCommentAndOwnership(postId, commentId, userId);

        commentService.updateContents(postId, commentId, editCommentReq.getContents());
    }




    /* ========================================================================================== */

    public void deleteComment(Long postId, Long commentId, Long userId) {
        validatePostCommentAndOwnership(postId, commentId, userId);

        commentService.deleteComment(postId, commentId);

    }

    /**
     * 게시물 작성자인지 검증 -> 수정, 삭제 시 확인 필요
     */
    private void validatePostOwner(Long postId, Long userId) {
        PostDTO post = validatePostExist(postId);

        // 게시물 작성자인지 확인
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "게시물에 대한 권한이 없습니다.");
        }
    }

    private void validatePostOwner(PostDTO post, Long userId) {
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }

        // 게시물 작성자인지 확인
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "게시물에 대한 권한이 없습니다.");
        }
    }

    /**
     * 게시물, 댓글 존재 여부 검증 댓글 작성자인지 검증
     */
    private void validatePostCommentAndOwnership(Long postId, Long commentId, Long userId) {
        // 게시물 존재 여부 검증
        validatePostExist(postId);

        // 댓글 존재 여부 검증
        CommentDTO comment = commentService.findByIdAndPostId(postId, commentId);
        if (comment == null) {
            throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 댓글 작성자인지 검증
        if (!userId.equals(comment.getUserId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS, "댓글에 대한 권한이 없습니다.");
        }

    }
}
