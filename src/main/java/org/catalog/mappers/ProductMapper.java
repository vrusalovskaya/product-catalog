package org.catalog.mappers;

import org.catalog.entities.ProductEntity;
import org.catalog.records.Product;
import org.catalog.records.ProductSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, WarehouseInventoryMapper.class, ReviewMapper.class}
)
public interface ProductMapper {

    @Mapping(target = "category", source = "categoryEntity")
    @Mapping(target = "inventory", source = "inventoryEntity")
    @Mapping(target = "reviews", source = "reviewEntities")
    Product toProduct(ProductEntity entity);

    List<Product> toProductList(List<ProductEntity> entities);

    @Mapping(target = "categoryEntity", source = "category")
    @Mapping(target = "inventoryEntity", source = "inventory")
    @Mapping(target = "reviewEntities", source = "reviews")
    ProductEntity toEntity(Product product);
}
