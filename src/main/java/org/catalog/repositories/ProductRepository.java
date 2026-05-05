package org.catalog.repositories;

import jakarta.persistence.Tuple;
import org.catalog.entities.ProductEntity;
import org.catalog.records.ProductSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<ProductEntity> findById(Long id);

    Optional<ProductEntity> findBySku(String sku);

    List<ProductEntity> search(ProductSearchCriteria criteria);

    List<ProductEntity> findTopRated(int limit);

    List<Tuple> getProductSummaries();
}
