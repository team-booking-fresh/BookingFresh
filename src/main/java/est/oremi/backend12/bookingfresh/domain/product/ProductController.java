package est.oremi.backend12.bookingfresh.domain.product;

import est.oremi.backend12.bookingfresh.domain.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "상품 목록 조회 서비스 API",
    description = "BookingFresh 상품 조회 API"
)
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  // 전체 상품 페이징 조회
  @Operation(summary = "전체 상품 조회",description = "모든 상품 리스트를 조회합니다.")
  @GetMapping
  public Page<ProductResponse> getProducts(Pageable pageable) {
    return productService.findAll(pageable);
  }

  // 카테고리별 상품 페이징 조회
  @Operation(summary = "카테고리 상품 조회",description = "특정 카테고리를 기반으로 상품 리스트를 조회합니다.")
  @GetMapping("/category/{categoryId}")
  public Page<ProductResponse> getProductsByCategory(
      @PathVariable Long categoryId,
      Pageable pageable) {
    return productService.searchProductsByCategory(categoryId, pageable);
  }

  // 검색어 기반 상품 조회
  @Operation(summary = "상품 검색 조회",description = "검색어를 기반으로 상품 리스트를 조회합니다.")
  @GetMapping("/search")
  public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam String keyword,
      Pageable pageable) {
    Page<ProductResponse> products = productService.searchProductsByName(keyword, pageable);
    return ResponseEntity.ok(products);
  }
}

