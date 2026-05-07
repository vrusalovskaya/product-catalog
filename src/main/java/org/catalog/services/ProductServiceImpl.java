package org.catalog.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.ReviewEntity;
import org.catalog.entities.WarehouseInventoryEntity;
import org.catalog.mappers.ProductMapper;
import org.catalog.records.Product;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.catalog.records.Review;
import org.catalog.repositories.CategoryRepository;
import org.catalog.repositories.ProductRepository;
import org.catalog.repositories.WarehouseInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toProduct(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findBySku(String sku) {
        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toProduct(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> search(ProductSearchCriteria criteria) {
        return productMapper.toProductList(productRepository.search(criteria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findTopRated(int limit) {
        return productMapper.toProductList(productRepository.findTopRated(limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> getProductSummaries() {
        List<Tuple> rows = productRepository.getProductSummaries();
        return rows.stream().map(row -> new ProductSummary(
                row.get("id", Long.class),
                row.get("productName", String.class),
                row.get("productPrice", BigDecimal.class),
                row.get("sku", String.class),
                row.get("categoryName", String.class)
        )).toList();
    }

    @Override
    @Transactional
    public Product save(Product product) {
       ProductEntity savedEntity = productRepository.save(productMapper.toEntity(product));
       return productMapper.toProduct(savedEntity);
    }

    @Override
    @Transactional
    public Product update(Product product) {
        ProductEntity loadedEntity = productRepository.findWithFetch(product.id())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        loadedEntity.setName(product.name());
        loadedEntity.setPrice(product.price());
        loadedEntity.setSku(product.sku());

        CategoryEntity categoryReference = categoryRepository.getReference(product.category().id());
        loadedEntity.setCategoryEntity(categoryReference);

        WarehouseInventoryEntity inventoryReference = inventoryRepository.getReference(product.id());
        loadedEntity.setInventoryEntity(inventoryReference);

        updateReviews(product, loadedEntity);

        return productMapper.toProduct(loadedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.delete(id);
    }

    private void updateReviews(Product product, ProductEntity loadedEntity) {
        List<ReviewEntity> current = loadedEntity.getReviewEntities();
        List<Review> updated = product.reviews();

        current.removeIf(existing -> updated.stream()
                .noneMatch(r -> r.id().equals(existing.getId())));

        for (Review review : updated) {
            if (review.id() == null) {
                ReviewEntity reviewEntity = new ReviewEntity();
                reviewEntity.setContent(review.content());
                reviewEntity.setRating(review.rating());
                current.add(reviewEntity);
            }
        }

        for (ReviewEntity reviewEntity : current) {
            updated.stream()
                    .filter(r -> r.id() != null && r.id().equals(reviewEntity.getId()))
                    .findFirst()
                    .ifPresent(r -> {
                        reviewEntity.setContent(r.content());
                        reviewEntity.setRating(r.rating());
                    });
        }
    }
}
