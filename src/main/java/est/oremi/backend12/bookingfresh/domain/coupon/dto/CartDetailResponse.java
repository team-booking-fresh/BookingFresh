package est.oremi.backend12.bookingfresh.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CartDetailResponse {
    private List<CartItemDetailResponse> items; // 아래 DTO 리스트
    private int totalQuantity;
    private BigDecimal totalAmount;     // 총 상품 금액
    private BigDecimal totalDiscount;   // 총 할인 금액
    private BigDecimal finalAmount;     // 최종 결제 금액
}