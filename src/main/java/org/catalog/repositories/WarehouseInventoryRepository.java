package org.catalog.repositories;

import org.catalog.entities.WarehouseInventoryEntity;

public interface WarehouseInventoryRepository {
    WarehouseInventoryEntity getReference(Long id);
}
