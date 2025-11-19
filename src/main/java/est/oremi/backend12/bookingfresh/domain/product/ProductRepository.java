package est.oremi.backend12.bookingfresh.domain.product;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
  Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("""
    SELECT p
    FROM Product p
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Product> findByKeyword(@Param("keyword") String keyword);

    default List<Product> findByKeywords(List<String> keywords) {
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                // 여기서 길이 필터링
                .filter(k -> k.length() >= 2)   // 필요하면 allow-list 예외 추가
                .flatMap(k -> findByKeyword(k).stream())
                .distinct()
                .collect(Collectors.toList());
    }

  // 상품 ID로 상품을 조회할 때 Category 엔티티를 함께 로딩 (Fetch Join), FQCN는 import 문 생략 + 명시
  @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE p.id = :productId")
  Optional<Product> findByIdWithCategory(@org.springframework.data.repository.query.Param("productId") Long productId);
}

