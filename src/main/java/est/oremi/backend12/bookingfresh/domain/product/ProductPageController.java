package est.oremi.backend12.bookingfresh.domain.product;

import est.oremi.backend12.bookingfresh.domain.coupon.repository.CategoryRepository;
import est.oremi.backend12.bookingfresh.domain.product.dto.ProductResponse;
import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductPageController {

  private final ProductService productService;
  private final CategoryRepository categoryRepository;


  // 전체 상품 리스트 페이지
  @GetMapping
  public String showAllProducts(@PageableDefault(size = 24) Pageable pageable,
      Model model, HttpServletRequest request) {
    Page<ProductResponse> products = productService.findAll(pageable);
    List<Category> categories = categoryRepository.findAll(); // 카테고리 전체 조회

    model.addAttribute("products", products);
    model.addAttribute("categories", categories);

    model.addAttribute("isLoggedIn", isLoggedIn(request));
    return "index"; // 전체 상품 리스트 페이지
  }

  // 카테고리별 상품 리스트 (같은 index.html 사용)
  @GetMapping(params = "categoryId")
  public String showProductsByCategory(@RequestParam Long categoryId,
      @PageableDefault(size = 24) Pageable pageable,
      Model model, HttpServletRequest request) {
    Page<ProductResponse> products = productService.searchProductsByCategory(categoryId, pageable);
    List<Category> categories = categoryRepository.findAll(); // 카테고리 전체 조회

    model.addAttribute("isLoggedIn", isLoggedIn(request));

    model.addAttribute("selectedCategoryId", categoryId);
    model.addAttribute("products", products);
    model.addAttribute("categories", categories);

    return "index";
  }
  // 검색 결과 페이지
  @GetMapping("/search")
  public String searchProducts(
      @RequestParam(required = false) String keyword,
      @PageableDefault(size = 24) Pageable pageable,
      Model model,
      HttpServletRequest request) {

    // 검색어가 없거나 비어있으면 전체 상품 페이지로 리다이렉트
    if (keyword == null || keyword.trim().isEmpty()) {
      return "redirect:/products";
    }

    // 검색 실행
    Page<ProductResponse> products = productService.searchProductsByName(keyword, pageable);
    List<Category> categories = categoryRepository.findAll();

    model.addAttribute("products", products);
    model.addAttribute("categories", categories);
    model.addAttribute("searchKeyword", keyword); // 검색어 표시용
    model.addAttribute("isLoggedIn", isLoggedIn(request));

    return "index"; // 동일한 index.html 사용
  }


  // 상품 상세 페이지 렌더링
  @GetMapping("/{productId}")
  public String showProductDetail(@PathVariable Long productId, Model model,
                                  HttpServletRequest request) {
    Product product = productService.findById(productId);
    ProductResponse response = ProductResponse.fromEntity(product);
    model.addAttribute("product", response);
    model.addAttribute("isLoggedIn", isLoggedIn(request));
    return "product-detail";
  }


  private boolean isLoggedIn(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
