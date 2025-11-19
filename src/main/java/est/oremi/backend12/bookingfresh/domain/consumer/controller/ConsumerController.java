package est.oremi.backend12.bookingfresh.domain.consumer.controller;

import est.oremi.backend12.bookingfresh.config.jwt.JwtTokenProvider;
import est.oremi.backend12.bookingfresh.domain.consumer.Service.ConsumerService;
import est.oremi.backend12.bookingfresh.domain.consumer.dto.*;
import est.oremi.backend12.bookingfresh.domain.consumer.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.token.TokenService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Tag(
        name = "사용자 API",
        description = "회원가입, 로그인, 로그아웃과 사용자 정보 조회 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConsumerController {
    private final ConsumerService consumerService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입",description = "입력 데이터를 바탕으로 사용자 정보 조회, 유효성 검사 실시")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddConsumerRequest request,
                                    BindingResult bindingResult) {

        // DTO 유효성 검사 실패 시 필드별 에러 리스트 반환
        if (bindingResult.hasErrors()) {
            List<FieldErrorResponse> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errors", errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            ConsumerResponse response = consumerService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "로그인",description = "email 과 password 를 입력받고, 유효성 인증이후 로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            List<FieldErrorResponse> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            TokenResponse tokenResponse = consumerService.login(request);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(true) // HTTP 환경 테스트를 위해 false (운영 시 true)
                    .path("/") // api 경로에 쿠키 전송
                    .sameSite("Strict")
                    .maxAge(jwtTokenProvider.getRefreshTokenExpirationSeconds()) // RT 만료 시간
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            // 응답 Body에는 Access Token만 포함하여 반환
            return ResponseEntity.ok(
                    TokenResponse.builder()
                            .accessToken(tokenResponse.getAccessToken())
                            // Refresh Token은 Body에서 제거하거나 null 처리
                            // Body에 RT가 남아 있으면 프론트가 오용할 수 있으므로 제거하는 것이 좋음
                            .build()
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    //토큰 재발급 API - Refresh Token을 이용해 Access Token과 Refresh Token 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken") String refreshToken, // Body 대신 쿠키에서 RT 읽기
            HttpServletResponse response // 새 쿠키를 설정하기 위해 response 추가
    ) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            TokenResponse tokenResponse = consumerService.refreshToken(refreshToken);

            // Service에서 반환된 "새로운" Refresh Token으로 쿠키 생성
            ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(true) // 배포시 Https 설정 이후 ture 로 설정
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(jwtTokenProvider.getRefreshTokenExpirationSeconds())
                    .build();

            response.addHeader("Set-Cookie", cookie.toString()); // 응답 헤더에 새 RT 쿠키 추가

            // 응답 Body에는 "새로운" Access Token만 포함하여 반환
            return ResponseEntity.ok(
                    TokenResponse.builder()
                            .accessToken(tokenResponse.getAccessToken())
                            .build()
            );

        } catch (IllegalArgumentException e) {
            // Refresh Token 무효/만료 등 실패 시 (401 Unauthorized)
            // (로그가 ConsumerService에 이미 찍히므로 여기선 401만 반환)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "로그아웃",description = "Refresh Token DB 삭제 및 쿠키 무효화")
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {

        consumerService.logout(refreshToken);

        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/") // 모든 경로에서 쿠키 삭제
                .maxAge(0) // 만료 시간을 0으로 설정하여 즉시 삭제
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body("로그아웃 성공");
    }

    @PatchMapping("/me")
    public ResponseEntity<ConsumerResponse> updateConsumerInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ConsumerUpdateRequest request) {

        //현재 로그인된 사용자의 ID 가져옴
        Long consumerId = customUserDetails.getId();
        // 서비스 메서드 호출
        ConsumerResponse updatedConsumer = consumerService.updateConsumerInfo(
                consumerId,
                request
        );

        return ResponseEntity.ok(updatedConsumer);
    }

    @Operation(summary = "사용자 정보 조회",description = "사용자 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ConsumerResponse> getConsumerInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long consumerId = customUserDetails.getId();

        ConsumerResponse consumerInfo = consumerService.getConsumerInfo(consumerId);

        return ResponseEntity.ok(consumerInfo);
    }
}
