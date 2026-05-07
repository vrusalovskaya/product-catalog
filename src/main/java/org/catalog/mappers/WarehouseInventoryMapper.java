package org.catalog.mappers;

import org.catalog.entities.WarehouseInventoryEntity;
import org.catalog.records.WarehouseInventory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseInventoryMapper {
    WarehouseInventory toRecord(WarehouseInventoryEntity entity);

    WarehouseInventoryEntity toEntity(WarehouseInventory record);
}
