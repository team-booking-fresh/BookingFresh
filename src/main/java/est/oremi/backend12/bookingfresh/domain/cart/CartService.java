package est.oremi.backend12.bookingfresh.domain.cart;

import est.oremi.backend12.bookingfresh.domain.cart.dto.CartDto;
import est.oremi.backend12.bookingfresh.domain.cart.dto.CartItemDto;
import est.oremi.backend12.bookingfresh.domain.consumer.entity.Consumer;
import est.oremi.backend12.bookingfresh.domain.consumer.repository.ConsumerRepository;
import est.oremi.backend12.bookingfresh.domain.coupon.Coupon;
import est.oremi.backend12.bookingfresh.domain.coupon.UserCoupon;
import est.oremi.backend12.bookingfresh.domain.coupon.dto.CartDetailResponse;
import est.oremi.backend12.bookingfresh.domain.coupon.dto.CartItemDetailResponse;
import est.oremi.backend12.bookingfresh.domain.product.Product;
import est.oremi.backend12.bookingfresh.domain.product.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ConsumerRepository consumerRepository;
  private final ProductRepository productRepository;
  private final CartItemRepository cartItemRepository;

  // 장바구니에 상품 추가
  @Transactional
  public void addProductToCart(Long consumerId, Long productId, int quantity) {
    Consumer consumer = consumerRepository.findById(consumerId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

    Cart cart = cartRepository.findByConsumerId(consumerId)
        .orElseGet(() -> new Cart(consumer));

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));

    CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
        .orElseGet(() -> cart.addItem(product, quantity));
    // 이미 있던 경우에는 수량 누적
    if (item.getId() != null) {
      item.addQuantity(quantity);
    }
    cartRepository.save(cart);
  }

  //장바구니 물품 수량 변경
  @Transactional
  public void updateQuantity(Long consumerId, Long productId, int quantity) {

    Cart cart = cartRepository.findByConsumerId(consumerId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니 없음"));

    CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품 없음"));

    item.updateQuantity(quantity);
    cartRepository.save(cart);
  }


  // 장바구니에서 상품 제거
  @Transactional
  public void removeProductFromCart(Long consumerId, Long productId) {
    Cart cart = cartRepository.findByConsumerId(consumerId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니 없음"));

    CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품 없음"));

    cart.removeItem(item);
    cartItemRepository.delete(item);
  }

  // 장바구니 조회
  @Transactional
  public CartDto getCart(Long consumerId) {
    Cart cart = cartRepository.findByConsumerId(consumerId)
        .orElse(null);

    // 장바구니가 없으면 빈 DTO 반환
    if (cart == null || cart.getItems().isEmpty()) {
      return new CartDto(
          Collections.emptyList(),  // 빈 상품 목록
          0,                         // 총 개수 0
          BigDecimal.ZERO            // 총 금액 0
      );
    }
    List<CartItemDto> items = cart.getItems().stream()
        .map(item -> {
          Product p = item.getProduct();
          BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
          return new CartItemDto(
              p.getId(),
              p.getName(),
              p.getWeight_pieces(),
              item.getQuantity(),
              p.getPrice(),
              lineTotal,
              p.getPhotoUrl()
          );
        })
        .toList();

    int totalQuantity = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    BigDecimal totalAmount = items.stream()
        .map(CartItemDto::getLineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartDto(items, totalQuantity, totalAmount);
  }

  //장바구니 비우기
  @Transactional
  public void clearCart(Long consumerId) {
    Cart cart = cartRepository.findByConsumerId(consumerId)
        .orElseThrow(() -> new IllegalArgumentException("장바구니 없음"));

    cart.clear();
    cartRepository.save(cart);
  }

  // 쿠폰 정보가 포함된 장바구니 상세 조회 -> 쿠폰 정보를 알아야 최종 금액 산출 가능
  @Transactional
  public CartDetailResponse getCartDetails(Long consumerId) {
    Cart cart = cartRepository.findByConsumerId(consumerId).orElse(null);

    // 장바구니가 비어있을 때
    if (cart == null || cart.getItems().isEmpty()) {
      return CartDetailResponse.builder()
              .items(Collections.emptyList())
              .totalQuantity(0)
              .totalAmount(BigDecimal.ZERO)
              .totalDiscount(BigDecimal.ZERO)
              .finalAmount(BigDecimal.ZERO)
              .build();
    }

    // 아이템 변환 로직
    List<CartItemDetailResponse> items = cart.getItems().stream()
            .map(item -> {
              Product p = item.getProduct();
              UserCoupon userCoupon = item.getUserCoupon();

              BigDecimal originalPrice = p.getPrice();
              BigDecimal lineTotal = originalPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

              // 쿠폰 할인 계산
              BigDecimal discountedPrice = originalPrice;
              CartItemDetailResponse.AppliedCouponInfo couponInfo = null;

              if (userCoupon != null && userCoupon.getIsApplied()) {
                Coupon coupon = userCoupon.getCoupon();
                couponInfo = new CartItemDetailResponse.AppliedCouponInfo(userCoupon.getId(), coupon.getName());
                discountedPrice = calculateDiscountedPrice(originalPrice, coupon);
              }

              return CartItemDetailResponse.builder()
                      .cartItemId(item.getId())
                      .productId(p.getId())
                      .name(p.getName())
                      .weightPieces(p.getWeight_pieces())
                      .quantity(item.getQuantity())
                      .price(originalPrice)
                      .lineTotal(lineTotal)
                      .photoUrl(p.getPhotoUrl())
                      .priceAfterDiscount(discountedPrice)
                      .appliedCoupon(couponInfo)
                      .build();
            })
            .toList();

    // 전체 합계 계산
    BigDecimal totalAmount = items.stream()
            .map(CartItemDetailResponse::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal finalAmount = items.stream()
            .map(i -> i.getPriceAfterDiscount().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalDiscount = totalAmount.subtract(finalAmount);

    return CartDetailResponse.builder()
            .items(items)
            .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
            .totalAmount(totalAmount)
            .totalDiscount(totalDiscount)
            .finalAmount(finalAmount)
            .build();
  }

  // 쿠폰 가격 계산 헬퍼 메서드
  private BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, Coupon coupon) {
    // Coupon 엔티티의 getDiscountValue()가 String인지 BigDecimal인지에 따라 변환 필요
    BigDecimal discountVal = new BigDecimal(coupon.getDiscountValue());

    if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
      BigDecimal rate = discountVal.divide(BigDecimal.valueOf(100));
      BigDecimal discountAmount = originalPrice.multiply(rate);
      return originalPrice.subtract(discountAmount);
    } else {
      // 정액 할인
      return originalPrice.subtract(discountVal).max(BigDecimal.ZERO);
    }
  }
}



