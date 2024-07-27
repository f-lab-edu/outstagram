package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.common.constant.CacheConst.USER;
import static com.outstagram.outstagram.common.constant.KafkaConst.USER_DELETE_TOPIC;
import static com.outstagram.outstagram.common.constant.KafkaConst.USER_UPSERT_TOPIC;
import static com.outstagram.outstagram.util.SHA256Util.encryptedPassword;

import com.outstagram.outstagram.common.constant.DBConst;
import com.outstagram.outstagram.controller.request.EditUserReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.kafka.producer.UserProducer;
import com.outstagram.outstagram.mapper.UserMapper;
import com.outstagram.outstagram.util.Snowflake;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final Snowflake snowflake;
    private final UserMapper userMapper;
    private final UserProducer userProducer;

    // shardId가 1이면 userId % DB_COUNT == 1 인 userId만 존재하도록 만들기 위한 메서드
    private long generateId(long shardId) {
        long userId;
        do {
            userId = snowflake.nextId(shardId);
        } while (userId % DBConst.DB_COUNT != shardId);
        return userId;
    }

    @Transactional
    public void insertUser(Long shardId, UserDTO userInfo) {
        // 이메일, 닉네임 중 중복 체크
        validateUserInfo(userInfo);

        LocalDateTime now = LocalDateTime.now();

        long userId = generateId(shardId);

        userInfo.setId(userId);
        userInfo.setCreateDate(now);
        userInfo.setUpdateDate(now);
        userInfo.setPassword(encryptedPassword(userInfo.getPassword()));

        userMapper.insertUser(userInfo);    // mysql에 저장
        userProducer.save(USER_UPSERT_TOPIC, userInfo); // elasticsearch db에 저장
    }

    /**
     * userId로 유저 찾기
     */
    @Cacheable(value = USER, key = "#userId")
    public UserDTO getUser(Long userId) {
        UserDTO user = userMapper.findById(userId);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }


    /**
     * 로그인 메서드
     */
    public UserDTO login(String email, String password) {
        String cryptoPassword = encryptedPassword(password);
        return userMapper.findByEmailAndPassword(email, cryptoPassword);
    }

    //==validator method==//

    /**
     * 중복 -> true
     */
    public void validateDuplicatedEmail(String email) {
        int count = userMapper.countByEmail(email);
        if (count > 0) {
            throw new ApiException(ErrorCode.DUPLICATED);
        }
    }

    public void validateDuplicatedNickname(String nickname) {
        int count = userMapper.countByNickname(nickname);
        if (count > 0) {
            throw new ApiException(ErrorCode.DUPLICATED);
        }
    }

    public List<UserDTO> searchByNickname(String searchText) {
        return userMapper.findByNicknameContaining(searchText);
    }


    /* ========================================================================================== */

    /**
     * email, nickname 둘 다 중복되지 않을 경우 -> true
     */
    private void validateUserInfo(UserDTO userInfo) {
        validateDuplicatedEmail(userInfo.getEmail());
        validateDuplicatedNickname(userInfo.getNickname());
    }

    @Transactional
    public void editProfile(UserDTO currentUser, EditUserReq editUserReq) {
        currentUser.setNickname(editUserReq.getNickname());
        currentUser.setPassword(encryptedPassword(editUserReq.getPassword()));
        currentUser.setUpdateDate(LocalDateTime.now());

        userMapper.editProfile(currentUser);

        userProducer.edit(USER_UPSERT_TOPIC, currentUser);
    }

    @Transactional
    public void deleteUser(UserDTO user) {
        userMapper.deleteById(user.getId());
        userProducer.delete(USER_DELETE_TOPIC, user);
    }
}
