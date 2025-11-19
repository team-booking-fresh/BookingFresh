package est.oremi.backend12.bookingfresh.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRegistrationRequest {

    private String code;
    private String name;
    private String discountType;
    private String discountValue;
    private String minOrderAmount;
    private Boolean isActive;

    // CategoryCoupon 매핑을 위한 필드
    private List<Long> categoryIds; // 쿠폰을 적용할 카테고리 ID 목록

}