package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.common.api.ApiResponse;
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
        List<SearchUserInfoRes> response = userService.searchByNickname(search);

        return ResponseEntity.ok(response);

    }

    @GetMapping("{userId}")
    public ResponseEntity<UserInfoRes> getUser(@PathVariable Long userId) {
        UserInfoRes response = userService.getUser(userId);

        return ResponseEntity.ok(response);
    }

}
