package org.catalog.repositories;

import org.catalog.entities.CategoryEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CategoryRepository {
    CategoryEntity findRoots();

    List<CategoryEntity> findSubCategories(Long parentId);

    List<CategoryEntity> getByProductPrice(BigDecimal price);
}
