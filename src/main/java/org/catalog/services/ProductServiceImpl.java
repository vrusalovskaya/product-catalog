package org.catalog.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.catalog.entities.ProductEntity;
import org.catalog.mappers.ProductMapper;
import org.catalog.records.Product;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.catalog.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
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
}
