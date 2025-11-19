package est.oremi.backend12.bookingfresh.domain.product;

import est.oremi.backend12.bookingfresh.domain.consumer.entity.CustomUserDetails;
import est.oremi.backend12.bookingfresh.domain.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(
    name = "상품 찜,좋아요 서비스 API",
    description = "BookingFresh 찜,좋아요 담당 API"
)
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductLikeController {

  private final ProductLikeService productLikeService;

  // 좋아요 생성
  @PostMapping("/{productId}/like")
  public ResponseEntity<Void> likeProduct(@PathVariable Long productId,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    productLikeService.likeProduct(customUserDetails.getId(), productId);
    return ResponseEntity.ok().build();
  }
  // 좋아요 삭제
  @DeleteMapping("/{productId}/like")
  public ResponseEntity<Void> unlikeProduct(@PathVariable Long productId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    productLikeService.unlikeProduct(customUserDetails.getId(), productId);
    return ResponseEntity.ok().build();
  }

  //좋아요 표시한 목록 조회
  @Operation(summary = "찜한 상품 조회",description = "사용자가 찜한 상품 리스트를 조회합니다.")
  @GetMapping("/likes")
  public ResponseEntity<List<ProductResponse>> getLikedProducts(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    List<Product> products = productLikeService.getLikedProducts(customUserDetails.getId());
    List<ProductResponse> productResponses = products.stream()
        .map(ProductResponse::fromEntity) // Product → ProductResponse 변환
        .toList();
    return ResponseEntity.ok(productResponses);
  }

}
