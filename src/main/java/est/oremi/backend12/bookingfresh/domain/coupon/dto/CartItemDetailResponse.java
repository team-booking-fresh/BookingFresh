package est.oremi.backend12.bookingfresh.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CartItemDetailResponse {

    private Long cartItemId;  // 쿠폰 적용을 위한 PK

    private Long productId;
    private String name;
    private String weightPieces;
    private int quantity;
    private BigDecimal price;         // 원가
    private BigDecimal lineTotal;     // 원가 합계
    private String photoUrl;

    private BigDecimal priceAfterDiscount; // 쿠폰 적용 후 단가
    private AppliedCouponInfo appliedCoupon; // 적용된 쿠폰 정보

    @Getter
    @AllArgsConstructor
    public static class AppliedCouponInfo {
        private Long userCouponId;
        private String name;
    }
}