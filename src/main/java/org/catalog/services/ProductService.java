package org.catalog.services;

import org.catalog.records.Product;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;

import java.util.List;

public interface ProductService {

    Product findById(Long id);

    List<Product> search(ProductSearchCriteria criteria);

    List<Product> findTopRated(int limit);

    List<ProductSummary> getProductSummaries();
}
