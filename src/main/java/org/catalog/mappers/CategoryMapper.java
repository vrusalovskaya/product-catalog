package org.catalog.mappers;

import org.catalog.entities.CategoryEntity;
import org.catalog.records.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parentEntity.id")
    @Mapping(target = "parentName", source = "parentEntity.name")
    Category toRecord(CategoryEntity entity);
}
