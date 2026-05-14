package org.catalog.services;

import org.catalog.records.Review;

public interface ReviewService {
    Review create(Long productId, Review review);

    Review update(Long productId, Long reviewId, Review review);

    void delete(Long productId, Long reviewId);
}
