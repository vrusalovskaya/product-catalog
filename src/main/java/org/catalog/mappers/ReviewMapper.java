package org.catalog.mappers;

import org.catalog.dtos.ReviewDto;
import org.catalog.entities.ReviewEntity;
import org.catalog.records.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
    @Mapping(target = "productId", source = "productEntity.id")
    Review toRecord(ReviewEntity entity);

    @Mapping(target = "productEntity.id", source = "productId")
    ReviewEntity toEntity(Review review);

    Review toRecord(ReviewDto reviewDto);
}
