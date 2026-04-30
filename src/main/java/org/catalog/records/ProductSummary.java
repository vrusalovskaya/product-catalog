package org.catalog.records;

import java.math.BigDecimal;

public record ProductSummary(
        Long id,
        String name,
        BigDecimal price,
        String sku,
        String categoryName
) {
}
