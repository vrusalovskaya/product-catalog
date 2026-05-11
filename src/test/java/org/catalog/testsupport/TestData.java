package org.catalog.testsupport;

import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.ReviewEntity;
import org.catalog.entities.WarehouseInventoryEntity;

import java.math.BigDecimal;

public final class TestData {

    private TestData() {
    }

    public static CategoryEntity category(String name) {
        CategoryEntity c = new CategoryEntity();
        c.setName(name);
        return c;
    }

    public static CategoryEntity category(String name, CategoryEntity parent) {
        CategoryEntity c = category(name);
        c.setParentEntity(parent);
        return c;
    }

    public static ProductEntity product(String name, String sku, BigDecimal price, CategoryEntity category) {
        ProductEntity p = new ProductEntity();
        p.setName(name);
        p.setSku(sku);
        p.setPrice(price);
        p.setCategoryEntity(category);

        WarehouseInventoryEntity inventory = new WarehouseInventoryEntity();
        inventory.setQuantity(10);
        inventory.setLocation("Warehouse A");
        p.setInventoryEntity(inventory);

        return p;
    }

    public static ReviewEntity review(String content, int rating) {
        ReviewEntity r = new ReviewEntity();
        r.setContent(content);
        r.setRating(rating);
        return r;
    }
}
