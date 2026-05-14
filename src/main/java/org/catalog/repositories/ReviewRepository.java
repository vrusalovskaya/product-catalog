package org.catalog.repositories;

import org.catalog.entities.ReviewEntity;

import java.util.Optional;

public interface ReviewRepository {
    ReviewEntity save(ReviewEntity review);

    Optional<ReviewEntity> findByIdAndProductId(Long reviewId, Long productId);

    void delete(Long productId, Long reviewId);
}
