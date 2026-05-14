package org.catalog.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.ReviewEntity;
import org.catalog.mappers.ReviewMapper;
import org.catalog.records.Review;
import org.catalog.repositories.ProductRepository;
import org.catalog.repositories.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public Review create(Long productId, Review review) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ReviewEntity reviewEntity = reviewMapper.toEntity(review);
        reviewEntity.setProductEntity(productEntity);

        ReviewEntity saved = reviewRepository.save(reviewEntity);
        return reviewMapper.toRecord(saved);
    }

    @Override
    @Transactional
    public Review update(Long productId, Long reviewId, Review review) {
        ReviewEntity loadedEntity = reviewRepository.findByIdAndProductId(reviewId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        loadedEntity.setRating(review.rating());
        loadedEntity.setContent(review.content());

        return reviewMapper.toRecord(loadedEntity);
    }

    @Override
    @Transactional
    public void delete(Long productId, Long reviewId) {
        reviewRepository.delete(productId, reviewId);
    }
}
