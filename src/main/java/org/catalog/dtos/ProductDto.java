package org.catalog.dtos;

import org.catalog.records.Category;
import org.catalog.records.WarehouseInventory;

import java.math.BigDecimal;

public record ProductDto(
        String name,
        BigDecimal price,
        String sku,
        Category category,
        WarehouseInventory inventory
) {
}
