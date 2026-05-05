package org.catalog.repositories;

import org.catalog.entities.WarehouseInventoryEntity;

public interface WarehouseInventoryRepository {
    WarehouseInventoryEntity findById(Long id);
}
