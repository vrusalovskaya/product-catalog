package org.catalog.mappers;

import org.catalog.entities.ReviewEntity;
import org.catalog.records.Review;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
    Review toRecord(ReviewEntity entity);
}
