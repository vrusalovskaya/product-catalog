package org.catalog.repositories;

import org.catalog.entities.CategoryEntity;

public interface CategoryRepository {
    CategoryEntity getReference(Long id);
}
