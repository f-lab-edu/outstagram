package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.EditUserReq;
import com.outstagram.outstagram.controller.request.UserLoginReq;
import com.outstagram.outstagram.controller.response.SearchUserInfoRes;
import com.outstagram.outstagram.controller.response.UserInfoRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("check-duplicated-email")
    public ResponseEntity<ApiResponse> isDuplicatedEmail(@RequestParam String email) {
        userService.validateDuplicatedEmail(email);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
                .message("해당 이메일 사용 가능합니다.").build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("check-duplicated-nickname")
    public ResponseEntity<ApiResponse> isDuplicatedNickName(@RequestParam String nickname) {
        userService.validateDuplicatedNickname(nickname);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
                .message("해당 닉네임이 사용 가능합니다.").build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody @Valid UserDTO userInfo) {
        userService.insertUser(userInfo);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
                .message("회원가입 성공").build();
        return ResponseEntity.ok(response);
    }


    /**
     * 세션 로그인 처리
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid UserLoginReq userLoginReq,
                                             HttpServletRequest request) {

        UserDTO user = userService.login(userLoginReq.getEmail(), userLoginReq.getPassword());
        log.info("loginUser = {}", user);

        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        // 로그인 성공 처리

        // 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(LOGIN_USER, user);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
                .message("로그인 성공").build();

        return ResponseEntity.ok(response);

    }

    /**
     * 닉네임으로 유저 검색
     */
    @GetMapping("/nicknames")
    public ResponseEntity<List<SearchUserInfoRes>> searchNickname(@RequestParam String search) {
        List<UserDTO> userList = userService.searchByNickname(search);

        List<SearchUserInfoRes> response = userList.stream()
                .map(doc -> SearchUserInfoRes.builder()
                        .userId(doc.getId())
                        .nickname(doc.getNickname())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoRes> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUser(userId);

        UserInfoRes response = UserInfoRes.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 내 정보 수정을 위한 내 정보 불러오기
     */
    @GetMapping("/profile")
    public ResponseEntity<UserInfoRes> getProfile(@Login UserDTO user) {
        UserInfoRes response = UserInfoRes.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse> editProfile(
            @Login UserDTO user,
            @RequestBody @Valid EditUserReq editUserReq
    ) {
        userService.editProfile(user, editUserReq);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .isSuccess(true)
                        .httpStatus(HttpStatus.OK)
                        .message("유저 프로필 수정 완료했습니다.")
                        .build()
        );
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        session.invalidate();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .isSuccess(true)
                        .httpStatus(HttpStatus.OK)
                        .message("정상적으로 로그아웃 되었습니다.")
                        .build()
        );
    }

    @DeleteMapping()
    public ResponseEntity<ApiResponse> deleteUser(@Login UserDTO user, HttpSession session) {
        userService.deleteUser(user);
        session.invalidate();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .isSuccess(true)
                        .httpStatus(HttpStatus.OK)
                        .message("유저 탈퇴 처리 완료되었습니다.")
                        .build()
        );
    }
}