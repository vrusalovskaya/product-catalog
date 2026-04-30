package org.catalog.repositories;

import jakarta.persistence.Tuple;
import org.catalog.entities.ProductEntity;
import org.catalog.records.ProductSearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository {
    ProductEntity findById(Long id);

    List<ProductEntity> search(ProductSearchCriteria criteria);

    List<ProductEntity> findTopRated(int limit);

    List<Tuple> getProductSummaries();
}
