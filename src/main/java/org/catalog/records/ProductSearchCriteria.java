package org.catalog.records;

import java.math.BigDecimal;

public record ProductSearchCriteria(
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sku,
        String categoryName,
        Integer minStock
) {
}
