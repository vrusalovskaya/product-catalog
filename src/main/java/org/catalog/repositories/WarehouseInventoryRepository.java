package org.catalog.repositories;

import org.catalog.entities.WarehouseInventoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseInventoryRepository {
    WarehouseInventoryEntity findById(Long id);
}
