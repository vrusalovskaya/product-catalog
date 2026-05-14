package org.catalog.repositories;

import org.catalog.entities.ProductEntity;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<ProductEntity> findById(Long id);

    Optional<ProductEntity> findWithFetch(Long id);

    Optional<ProductEntity> findBySku(String sku);

    List<ProductEntity> search(ProductSearchCriteria criteria);

    List<ProductEntity> findTopRated(int limit);

    List<ProductSummary> getProductSummaries();

    ProductEntity save(ProductEntity entity);

    void delete(Long id);
}
