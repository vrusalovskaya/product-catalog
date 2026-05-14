package org.catalog.records;

import java.math.BigDecimal;
import java.util.List;

public record Product(
        Long id,
        String name,
        BigDecimal price,
        String sku,
        Category category,
        WarehouseInventory inventory,
        List<Review> reviews
) {
}
