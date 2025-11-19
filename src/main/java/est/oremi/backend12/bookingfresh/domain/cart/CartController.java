package est.oremi.backend12.bookingfresh.domain.cart;

import est.oremi.backend12.bookingfresh.domain.cart.dto.CartDto;
import est.oremi.backend12.bookingfresh.domain.consumer.entity.CustomUserDetails;
import est.oremi.backend12.bookingfresh.domain.coupon.dto.CartDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@Tag(
    name = "장바구니 서비스 API",
    description = "BookingFresh 장바구니 기능 API"
)
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @Operation(summary = "장바구니 조회",description = "로그인된 사용자의 장바구니를 조회합니다.")
  @GetMapping
  public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal CustomUserDetails user) {
    Long consumerId = user.getId();
    CartDto cart = cartService.getCart(consumerId);
    return ResponseEntity.ok(cart);
  }

  @Operation(summary = "장바구니 상품 추가",description = "사용자의 장바구니에 상품을 추가합니다.")
  @PostMapping("/add")
  public ResponseEntity<Void> addProductToCart(@AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long productId,
      @RequestParam int quantity) {
    Long consumerId = user.getId();
    cartService.addProductToCart(consumerId, productId, quantity);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니 상품 수량 변경",description = "장바구니에 담긴 상품의 수량을 변경합니다.")
  @PatchMapping("/update")
  public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long productId,
      @RequestParam int quantity) {
    Long consumerId = user.getId();
    cartService.updateQuantity(consumerId, productId, quantity);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니 상품 삭제",description = "장바구니에 담긴 상품을 삭제합니다.")
  @DeleteMapping("/remove")
  public ResponseEntity<Void> removeProductFromCart(@AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long productId) {
    Long consumerId = user.getId();
    cartService.removeProductFromCart(consumerId, productId);
    return ResponseEntity.ok().build();
  }


  @Operation(summary = "장바구니 비우기",description = "장바구니를 초기화합니다.")
  @DeleteMapping("/clear")
  public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails user) {
    Long consumerId = user.getId();
    cartService.clearCart(consumerId);
    return ResponseEntity.ok().build();
  }

  //  장바구니 페이지용 상세 조회
  @Operation(summary = "장바구니 세부 조회",description = "로그인된 사용자의 장바구니를 장바구니 ID, 장바구니 목록 ID, 상품 ID 를 조회합니다.")
  @GetMapping("/detail")
  public ResponseEntity<CartDetailResponse> getCartDetails(@AuthenticationPrincipal CustomUserDetails user) {
    Long consumerId = user.getId();
    return ResponseEntity.ok(cartService.getCartDetails(consumerId));
  }
}

