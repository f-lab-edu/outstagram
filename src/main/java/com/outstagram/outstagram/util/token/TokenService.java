package com.outstagram.outstagram.util.token;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenHelper jwtTokenHelper;


    public TokenResDTO issueToken(UserDTO user) {
        return Optional.ofNullable(user)
            .map(entity -> {
                Long userId = user.getId();
                HashMap<String, Object> data = new HashMap<>();
                data.put("userId", userId);

                TokenDTO accessToken = jwtTokenHelper.issueAccessToken(data);
                TokenDTO refreshToken = jwtTokenHelper.issueRefreshToken(data);

                return toResponse(accessToken, refreshToken);
            })
            .orElseThrow(()-> new ApiException(ErrorCode.NULL_POINT));
    }

    public Long validationAccessToken(String token) {
        Map<String, Object> result = jwtTokenHelper.validationTokenWithThrow(token);
        Object userId = result.get("userId");

        // null check
        Objects.requireNonNull(userId, () -> {
            throw new ApiException(ErrorCode.NULL_POINT);
        });

        return Long.parseLong(userId.toString());
    }

    private TokenResDTO toResponse(
        TokenDTO accessToken,
        TokenDTO refreshToken
    ) {
        // accessToken과 refreshToken에 대해서 null check, null 발생시 해당 exception 던짐
        Objects.requireNonNull(accessToken, () -> {throw new ApiException(ErrorCode.NULL_POINT);});
        Objects.requireNonNull(refreshToken, () -> {throw new ApiException(ErrorCode.NULL_POINT);});

        return TokenResDTO.builder()
            .accessToken(accessToken.getToken())
            .accessTokenExpiredAt(accessToken.getExpiredAt())
            .refreshToken(refreshToken.getToken())
            .refreshTokenExpiredAt(refreshToken.getExpiredAt())
            .build();
    }


}
