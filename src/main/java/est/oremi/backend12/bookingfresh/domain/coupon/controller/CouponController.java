package est.oremi.backend12.bookingfresh.domain.coupon.controller;

import est.oremi.backend12.bookingfresh.domain.consumer.entity.CustomUserDetails;
import est.oremi.backend12.bookingfresh.domain.coupon.Coupon;
import est.oremi.backend12.bookingfresh.domain.coupon.dto.*;
import est.oremi.backend12.bookingfresh.domain.coupon.service.CartCouponService;
import est.oremi.backend12.bookingfresh.domain.coupon.service.CouponService;
import est.oremi.backend12.bookingfresh.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Tag(
        name = "쿠폰 생성, 조회, 상태변경 API",
        description = "카테고리, 상품에 적용 가능한 쿠폰을 조회하거나, 사용자 회원가입 시에 쿠폰을 발급하는 API"
)
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CartCouponService cartCouponService;

    @Operation(summary = "새로운 쿠폰 등록",description = "새로운 쿠폰을 등록하고 사용자에게 비동기 발급 - API 로만 동작")
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerCoupon(@RequestBody CouponRegistrationRequest request) {

        try {
            Coupon newCoupon = couponService.registerNewCoupon(request);

            Map<String, Object> response = new HashMap<>();
            response.put("id", newCoupon.getId());
            response.put("code", newCoupon.getCode());
            response.put("message", "쿠폰이 성공적으로 등록되었으며, 모든 사용자에게 비동기적으로 발급 중입니다.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // 카테고리 ID 오류 등 유효성 검사 실패 시
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "요청 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "쿠폰 목록 조회",description = "쿠폰과 쿠폰이 적용 가능한 모든 카테고리 조회")
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponResponse> response = couponService.findAllCouponsWithCategories();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 쿠폰 조회",description = "사용자가 소유한 모든 쿠폰 조회")
    @GetMapping("/my")
    public ResponseEntity<List<UserCouponResponse>> getAvailableUserCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long consumerId = userDetails.getId();
        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

         List<UserCouponResponse> response = couponService.findAvailableUserCoupons(consumerId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품에 대한 쿠폰 조회",description = "사용자가 소유하고, 상품을 대상으로 적용 가능한(카테고리가 일치하는) 쿠폰 조회")
    @GetMapping("/applicable-to/{productId}")
    public ResponseEntity<List<UserCouponProductResponse>> getApplicableUserCouponsWithPrice(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long consumerId = userDetails.getId();

        if (productId == null || consumerId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<UserCouponProductResponse> response = couponService.findApplicableCouponsForUserAndProductWithPrice(consumerId, productId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쿠폰의 상태 변경",description = "장바구니의 상품에 쿠폰을 적용(상태 변경)")
    @PatchMapping("/cart/item/coupon")
    public ResponseEntity<String> toggleCartItemCoupon(
            @RequestBody CouponCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long consumerId = userDetails.getId();
        try {
            cartCouponService.toggleCartItemCouponApplication(request, consumerId);

            String action = (request.getUserCouponId() != null && request.getUserCouponId() > 0) ? "적용" : "해제";
            return ResponseEntity.ok(request.getCartItemId() + "번 항목에 쿠폰이 성공적으로 " + action + "되었습니다.");

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | SecurityException | IllegalArgumentException e) {
            // 중복 사용, 사용 완료, 권한 없음, 상품 미적용 가능성 등의 오류
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 적용 중 오류 발생: " + e.getMessage());
        }
    }
}