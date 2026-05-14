package org.catalog.controllers;

import lombok.RequiredArgsConstructor;
import org.catalog.mappers.ReviewMapper;
import org.catalog.records.Review;
import org.catalog.dtos.ReviewDto;
import org.catalog.services.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @PostMapping
    public ResponseEntity<Review> create(@PathVariable Long productId, @RequestBody ReviewDto reviewDto) {
        Review savedReview = reviewService.create(productId, reviewMapper.toRecord(reviewDto));

        URI location = URI.create(String.format("/api/v1/products/%d/reviews/%d",
                                    savedReview.productId(), savedReview.id()));
        return ResponseEntity.created(location).body(savedReview);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> update(@PathVariable Long productId,
                                         @PathVariable Long reviewId,
                                         @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.update(productId, reviewId, reviewMapper.toRecord(reviewDto)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId, @PathVariable Long reviewId) {
        reviewService.delete(productId, reviewId);

        return ResponseEntity.noContent().build();
    }
}
