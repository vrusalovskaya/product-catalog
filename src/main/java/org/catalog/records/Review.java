package org.catalog.records;

public record Review(
        Long id,
        String content,
        Integer rating,
        Long productId
) {
}
