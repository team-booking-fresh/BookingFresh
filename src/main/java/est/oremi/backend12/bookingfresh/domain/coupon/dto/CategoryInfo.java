package est.oremi.backend12.bookingfresh.domain.coupon.dto;

import est.oremi.backend12.bookingfresh.domain.product.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfo {
    private Long id;
    private String name;

    public static CategoryInfo from(Category category) {
        return new CategoryInfo(
                category.getId(),
                category.getCategoryName().name()
        );
    }
}