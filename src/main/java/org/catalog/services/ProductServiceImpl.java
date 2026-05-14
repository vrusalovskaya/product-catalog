package org.catalog.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.WarehouseInventoryEntity;
import org.catalog.mappers.ProductMapper;
import org.catalog.records.*;
import org.catalog.repositories.CategoryRepository;
import org.catalog.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toRecord(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findBySku(String sku) {
        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toRecord(entity);
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
        return productRepository.getProductSummaries();
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);

        CategoryEntity categoryRef = categoryRepository.getReference(product.category().id());
        entity.setCategoryEntity(categoryRef);

        ProductEntity saved = productRepository.save(entity);
        return productMapper.toRecord(saved);
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        ProductEntity loadedEntity = productRepository.findWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        loadedEntity.setName(product.name());
        loadedEntity.setPrice(product.price());
        loadedEntity.setSku(product.sku());

        CategoryEntity categoryReference = categoryRepository.getReference(product.category().id());
        loadedEntity.setCategoryEntity(categoryReference);

        WarehouseInventoryEntity inventory = loadedEntity.getInventoryEntity();
        inventory.setQuantity(product.inventory().quantity());
        inventory.setLocation(product.inventory().location());

        return productMapper.toRecord(loadedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.delete(id);
    }

}
