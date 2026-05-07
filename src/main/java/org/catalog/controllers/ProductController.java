package org.catalog.controllers;

import lombok.RequiredArgsConstructor;
import org.catalog.records.Product;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.catalog.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping(params = "sku")
    public Product getBySku(@RequestParam String sku) {
        return productService.findBySku(sku);
    }

    @GetMapping("/search")
    public List<Product> search(@ModelAttribute ProductSearchCriteria criteria) {
        return productService.search(criteria);
    }

    @GetMapping("/top-rated")
    public List<Product> getTopRated(@RequestParam(defaultValue = "10") Integer limit) {
        return productService.findTopRated(limit);
    }

    @GetMapping("/summary")
    public List<ProductSummary> getProductSummary() {
        return productService.getProductSummaries();
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product savedProduct = productService.save(product);

        URI location = URI.create(String.format("/api/v1/products/%d", savedProduct.id()));
        return ResponseEntity.created(location).body(savedProduct);
    }

    @PutMapping
    public ResponseEntity<Product> update(@RequestBody Product product) {
        Product updatedProduct = productService.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
