package est.oremi.backend12.bookingfresh.config.jwt;

import est.oremi.backend12.bookingfresh.domain.consumer.Service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-milliseconds}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration-milliseconds}")
    private long REFRESH_TOKEN_EXPIRATION;

    private Key key;

    @PostConstruct
    public void init() {
        // Base64 인코딩된 시크릿 키를 Key 객체로 변환
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Refresh Token의 만료 시간 (초 단위)
    public long getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXPIRATION / 1000;
    }

    // Access/Refresh Token 생성 메서드
    public String generateToken(String email, String role, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(email) // 토큰 주인
                .claim("role", role)
                .setIssuedAt(now) // 발급 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명
                .compact();
    }

    public String generateAccessToken(String email, String role) {
        return generateToken(email, role, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email, String role) {
        return generateToken(email, role, REFRESH_TOKEN_EXPIRATION);
    }

    // 토큰에서 이메일(Subject, 사용자 정보) 추출
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 권한(role) 추출
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }  catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰입니다.");
        } catch (SignatureException e) {
            log.error("JWT 서명이 유효하지 않습니다.");
        } catch (UsernameNotFoundException e) {
            log.error("사용자를 찾지 못했습니다.");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    // 토큰 만료 시간 가져오기
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }
}