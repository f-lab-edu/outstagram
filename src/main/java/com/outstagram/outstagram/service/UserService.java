package com.outstagram.outstagram.service;

import com.outstagram.outstagram.controller.request.EditUserReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.kafka.producer.UserProducer;
import com.outstagram.outstagram.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.outstagram.outstagram.common.constant.CacheConst.USER;
import static com.outstagram.outstagram.common.constant.KafkaConst.*;
import static com.outstagram.outstagram.util.SHA256Util.encryptedPassword;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserProducer userProducer;

    /**
     * 유저 회원가입 메서드 비밀번호는 sha256으로 암호화해 저장
     */
    @Transactional
    public void insertUser(UserDTO userInfo) {

        // 이메일, 닉네임 중 중복 체크
        validateUserInfo(userInfo);

        userInfo.setCreateDate(LocalDateTime.now());
        userInfo.setUpdateDate(LocalDateTime.now());

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
