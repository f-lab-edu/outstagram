package com.outstagram.outstagram.controller;

import static com.outstagram.outstagram.common.constant.SessionConst.LOGIN_USER;

import com.outstagram.outstagram.common.annotation.Login;
import com.outstagram.outstagram.common.api.ApiResponse;
import com.outstagram.outstagram.controller.request.UserLoginReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.FollowService;
import com.outstagram.outstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @GetMapping("check-duplicated-email")
    public ResponseEntity<ApiResponse> isDuplicatedEmail(@RequestParam String email) {
        userService.validateDuplicatedEmail(email);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("해당 이메일 사용 가능합니다.").build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("check-duplicated-nickname")
    public ResponseEntity<ApiResponse> isDuplicatedNickName(@RequestParam String nickname) {
        userService.validateDuplicatedNickname(nickname);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("해당 닉네임이 사용 가능합니다.").build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody @Valid UserDTO userInfo) {
        userService.insertUser(userInfo);

        ApiResponse response = ApiResponse.builder().isSuccess(true).httpStatus(HttpStatus.OK)
            .message("회원가입 성공").build();
        return ResponseEntity.ok().body(response);
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

        return ResponseEntity.ok().body(response);
    }

    /* ========================================================================================== */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse> following(@PathVariable("userId") Long toId, @Login UserDTO user) {
        followService.addFollowing(user.getId(), toId);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("팔로우에 성공했습니다.")
                .build()
        );
    }


}
