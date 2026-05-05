package org.catalog.repositories;

import org.catalog.entities.CategoryEntity;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryRepository {
    CategoryEntity findRoots();

    List<CategoryEntity> findSubCategories(Long parentId);

    List<CategoryEntity> getByProductPrice(BigDecimal price);
}
