package com.outstagram.outstagram.service;

import com.outstagram.outstagram.common.constant.CacheNamesConst;
import com.outstagram.outstagram.controller.response.SearchUserInfoRes;
import com.outstagram.outstagram.controller.response.UserInfoRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.outstagram.outstagram.util.SHA256Util.encryptedPassword;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 유저 회원가입 메서드 비밀번호는 sha256으로 암호화해 저장
     */
    public void insertUser(UserDTO userInfo) {
        
        // 이메일, 닉네임 중 중복 체크
        validateUserInfo(userInfo);

        userInfo.setCreateDate(LocalDateTime.now());
        userInfo.setUpdateDate(LocalDateTime.now());

        userInfo.setPassword(encryptedPassword(userInfo.getPassword()));

        userMapper.insertUser(userInfo);
    }


    @Cacheable(value = CacheNamesConst.USER, key = "#userId")
    public UserInfoRes getUser(Long userId) {
        UserDTO user = userMapper.findById(userId);

        return UserInfoRes.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
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

    /**
     * userId로 유저 찾기
     */
    public UserDTO findByUserId(Long userId) {
        return userMapper.findById(userId);
    }



    /* ========================================================================================== */

    /**
     * email, nickname 둘 다 중복되지 않을 경우 -> true
     */
    private void validateUserInfo(UserDTO userInfo) {
        validateDuplicatedEmail(userInfo.getEmail());
        validateDuplicatedNickname(userInfo.getNickname());
    }

    public List<SearchUserInfoRes> searchByNickname(String search) {
        List<UserDTO> resultList = userMapper.findByNicknameContaining(search);

        return resultList.stream()
            .map(userDTO -> SearchUserInfoRes.builder()
                .userId(userDTO.getId())
                .nickname(userDTO.getNickname())
                .build())
            .collect(Collectors.toList());
    }

}
