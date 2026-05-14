package org.catalog.records;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductSearchCriteria(
        @Size(max = 50)
        String name,
        @PositiveOrZero
        BigDecimal minPrice,
        @PositiveOrZero
        BigDecimal maxPrice,
        @Size(max = 50)
        String sku,
        @Size(max = 50)
        String categoryName,
        @PositiveOrZero
        Integer minStock
) {
}
