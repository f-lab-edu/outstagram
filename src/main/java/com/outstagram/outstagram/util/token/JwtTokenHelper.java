package com.outstagram.outstagram.util.token;

import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenHelper {

    @Value("${token.secret.key}")
    private String secretKey;

    @Value("${token.access-token.plus-hour}")
    private Long accessTokenPlusHour;

    @Value("${token.refresh-token.plus-hour}")
    private Long refreshTokenPlusHour;

    public TokenDTO issueAccessToken(Map<String, Object> data) {
        return createToken(data, accessTokenPlusHour);
    }

    public TokenDTO issueRefreshToken(Map<String, Object> data) {
        return createToken(data, refreshTokenPlusHour);
    }

    public Map<String, Object> validationTokenWithThrow(String token) {
        // SecretKey 만들기
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        // token 파싱하고 검증하기
        JwtParser parser = Jwts.parserBuilder()
            .setSigningKey(key)
            .build();

        try {
            Jws<Claims> claimsJws = parser.parseClaimsJws(token);
            return new HashMap<>(claimsJws.getBody());
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw new ApiException(ErrorCode.INVALID_TOKEN);
            } else if (e instanceof ExpiredJwtException) {
                throw new ApiException(ErrorCode.EXPIRED_TOKEN);
            } else {
                throw new ApiException(ErrorCode.TOKEN_EXCEPTION);
            }
        }
    }

    private TokenDTO createToken(Map<String, Object> data, Long plusHour) {
        // 토큰 만료 시간 구하고 Date 타입으로 변환(JWT 라이브러리 맞춰야함)
        LocalDateTime expiredLDT = LocalDateTime.now().plusHours(plusHour);
        Date expiredAt = Date.from(expiredLDT.atZone(ZoneId.systemDefault()).toInstant());

        // token 서명에 사용할 SecretKey 객체 생성
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        // JWT token 생성
        String jwtToken = Jwts.builder()
            .signWith(key)
            .setClaims(data)
            .setExpiration(expiredAt)
            .compact();

        return TokenDTO.builder()
            .token(jwtToken)
            .expiredAt(expiredLDT)
            .build();
    }
}
