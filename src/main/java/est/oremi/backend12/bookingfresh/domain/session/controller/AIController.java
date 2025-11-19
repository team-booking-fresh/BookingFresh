package est.oremi.backend12.bookingfresh.domain.session.controller;

import est.oremi.backend12.bookingfresh.domain.consumer.entity.Consumer;
import est.oremi.backend12.bookingfresh.domain.consumer.entity.CustomUserDetails;
import est.oremi.backend12.bookingfresh.domain.session.Service.AIMessageService;
import est.oremi.backend12.bookingfresh.domain.session.Service.AIRecommendationService;
import est.oremi.backend12.bookingfresh.domain.session.Service.AISessionService;
import est.oremi.backend12.bookingfresh.domain.session.dto.*;
import est.oremi.backend12.bookingfresh.domain.session.entity.AiRecommendation;
import est.oremi.backend12.bookingfresh.domain.session.entity.Message;
import est.oremi.backend12.bookingfresh.domain.session.entity.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(
        name = "AI 서비스 API",
        description = "BooKingFresh AI 세션,메시지,상품추천 api"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {
    private final AISessionService aiSessionService;
    private final AIMessageService aiMessageService;
    private final AIRecommendationService aiRecommendationService;

    /* -----------------------------------------------------
     * 공통 인증 필요 함수
     * ----------------------------------------------------- */
    private Consumer requireUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getConsumer() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userDetails.getConsumer();
    }

    //    AI 세션 생성
    @Operation(summary = "세션 생성", description = "AI 서비스 세션을 생성합니다")
    @PostMapping("/sessions")
    public ResponseEntity<AiSessionResponse> startNewSession(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);

        Session session = aiSessionService.createSession(user);

        URI location = URI.create("/api/ai/sessions/" + session.getIdx());
        return ResponseEntity.created(location)
                .body(AiSessionResponse.from(session));
    }

    //세션 목록 조회
    @Operation(summary = "세션 목록 조회", description = "사용자의 AI 서비스 세션을 조회합니다")
    @GetMapping("/sessions")
    public ResponseEntity<List<AiSessionResponse>> getSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);

        return ResponseEntity.ok(
                aiSessionService.getUserSessions(user)
        );
    }

    //단일 세션 조회
    @Operation(summary = "단일 세션 조회", description = "유저의 특정 세션 하나를 조회합니다")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<AiSessionResponse> getSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);

        return ResponseEntity.ok(
                aiSessionService.getSessionDetail(sessionId, user)
        );
    }

    //메시지 전송
    @Operation(summary = "메시지 전송", description = "LLM 시스템에 메시지를 전송하고 응답을 받습니다")
    @PostMapping("/messages")
    public ResponseEntity<AiMessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AiMessageRequest request
    ) {
        Consumer user = requireUser(userDetails);

        return ResponseEntity.ok(
                aiMessageService.handleUserMessage(user, request)
        );
    }

    // 세션 내 메시지 목록 조회
    @Operation(summary = "세션 메시지 조회", description = "세션에 속한 메시지들을 조회합니다")
    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<AiMessageResponse>> getMessages(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);

        return ResponseEntity.ok(
                aiMessageService.getMessagesBySession(sessionId, user)
        );
    }

    //세션 삭제
    @Operation(summary = "세션 삭제", description = "한 서비스 세션을 삭제합니다")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);
        aiSessionService.deleteSession(sessionId, user);

        return ResponseEntity.noContent().build(); // 204 No Content
    }


    //AI 추천 상품 생성 API
    @Operation(summary = "상품 추천 생성", description = "AI 응답을 기반으로 상품 추천을 생성합니다")
    @PostMapping("/recommendations")
    public ResponseEntity<List<AiRecommendationResponse>> generateRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AiRecommendationRequest request
    ) {
        Consumer user = requireUser(userDetails);

        // 세션/메시지 조회
        Session session = aiSessionService.findByIdAndUser(request.getSessionId(), user);
        Message aiMsg = aiMessageService.findById(request.getMessageId());
        AiResponseData aiResponse = new AiResponseData(
                aiMsg.getIntent().name(),
                aiMsg.getStructuredJson(),       // parseRecipe 결과 JSON
                aiMsg.getContent()               // 원본 AI 텍스트
        );

        // 추천 생성
        List<AiRecommendation> recommendations =
                aiRecommendationService.generateRecommendations(session, aiMsg, aiResponse);

        // DTO 변환
        List<AiRecommendationResponse> responses = recommendations.stream()
                .map(AiRecommendationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // 세션 내 시스템 추천상품 목록 조회
    @Operation(summary = "세션 내 추천 조회", description = "특정 세션에서 발생한 상품 추천을들 조회합니다")
    @GetMapping("/recommendations/{sessionId}")
    public ResponseEntity<List<AiRecommendationResponse>> getRecommendationsBySession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Consumer user = requireUser(userDetails);

        // 세션 검증
        Session session = aiSessionService.findByIdAndUser(sessionId, user);

        // 추천 목록 조회
        List<AiRecommendation> recommendations = aiRecommendationService.getRecommendationsBySession(session);

        // DTO 변환
        List<AiRecommendationResponse> responses = recommendations.stream()
                .map(AiRecommendationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }


}
