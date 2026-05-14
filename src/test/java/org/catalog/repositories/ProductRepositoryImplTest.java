package org.catalog.repositories;

import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.ReviewEntity;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.catalog.testsupport.AbstractIntegrationTest;
import org.catalog.testsupport.TestData;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductRepositoryImplTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("findById returns the product when it exists")
    void findById_returnsProduct() {
        Long id = seedLaptopWithReview();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ProductEntity> result = productRepository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getSku()).isEqualTo("TP-X1-2026");
            assertThat(result.get().getName()).isEqualTo("ThinkPad X1");
        });
    }

    @Test
    @DisplayName("findById returns empty when no product exists")
    void findById_returnsEmpty_whenMissing() {
        transactionTemplate.executeWithoutResult(status ->
                assertThat(productRepository.findById(999_999L)).isEmpty());
    }

    @Test
    @DisplayName("findWithFetch eagerly loads category, inventory and reviews")
    void findWithFetch_loadsAssociations() {
        Long id = seedLaptopWithReview();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ProductEntity> result = productRepository.findWithFetch(id);
            assertThat(result).isPresent();
            ProductEntity p = result.get();

            assertThat(org.hibernate.Hibernate.isInitialized(p.getCategoryEntity())).isTrue();
            assertThat(org.hibernate.Hibernate.isInitialized(p.getInventoryEntity())).isTrue();
            assertThat(org.hibernate.Hibernate.isInitialized(p.getReviewEntities())).isTrue();
            assertThat(p.getReviewEntities()).hasSize(1);
            assertThat(p.getInventoryEntity().getLocation()).isEqualTo("Warehouse A");
        });
    }

    @Test
    @DisplayName("findBySku locates the product by its natural id")
    void findBySku_returnsProduct() {
        seedLaptopWithReview();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ProductEntity> result = productRepository.findBySku("TP-X1-2026");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("ThinkPad X1");
        });
    }

    @Test
    @DisplayName("findBySku returns empty for an unknown SKU")
    void findBySku_returnsEmpty_whenUnknown() {
        transactionTemplate.executeWithoutResult(status ->
                assertThat(productRepository.findBySku("does-not-exist")).isEmpty());
    }

    @Test
    @DisplayName("search filters by name fragment")
    void search_byNameFragment() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria("Think", null, null, null, null, null));
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getSku()).isEqualTo("TP-X1-2026");
        });
    }

    @Test
    @DisplayName("search filters by price range")
    void search_byPriceRange() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria(null,
                            new BigDecimal("100.00"),
                            new BigDecimal("500.00"),
                            null, null, null));
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getSku()).isEqualTo("MOUSE-1");
        });
    }

    @Test
    @DisplayName("search filters by exact SKU")
    void search_bySku() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria(null, null, null, "MOUSE-1", null, null));
            assertThat(result).extracting(ProductEntity::getName).containsExactly("Logi Mouse");
        });
    }

    @Test
    @DisplayName("search filters by category name")
    void search_byCategory() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria(null, null, null, null, "Laptops", null));
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getSku()).isEqualTo("TP-X1-2026");
        });
    }

    @Test
    @DisplayName("search filters by minimum stock")
    void search_byMinStock() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria(null, null, null, null, null, 50));
            assertThat(result).extracting(ProductEntity::getSku).containsExactly("TP-X1-2026");
        });
    }

    @Test
    @DisplayName("search returns all products when criteria is empty")
    void search_returnsAll_whenCriteriaEmpty() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> result = productRepository.search(
                    new ProductSearchCriteria(null, null, null, null, null, null));
            assertThat(result).hasSize(2);
        });
    }

    @Test
    @DisplayName("findTopRated returns products ordered by average rating desc")
    void findTopRated_orderedByAverageRating() {
        transactionTemplate.executeWithoutResult(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity cat = TestData.category("Electronics");
            s.persist(cat);

            ProductEntity high = TestData.product("High-rated", "HR-1",
                    new BigDecimal("100.00"), cat);
            high.addReviewEntity(TestData.review("great", 5, high));
            high.addReviewEntity(TestData.review("nice", 5, high));
            s.persist(high);

            ProductEntity low = TestData.product("Low-rated", "LR-1",
                    new BigDecimal("100.00"), cat);
            low.addReviewEntity(TestData.review("ok", 2, low));
            s.persist(low);
        });

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductEntity> top = productRepository.findTopRated(10);
            assertThat(top).extracting(ProductEntity::getSku)
                    .containsExactly("HR-1", "LR-1");
        });
    }

    @Test
    @DisplayName("findTopRated honours the limit argument")
    void findTopRated_respectsLimit() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status ->
                assertThat(productRepository.findTopRated(1)).hasSize(1));
    }

    @Test
    @DisplayName("getProductSummaries returns id, name, price, sku and category name tuples")
    void getProductSummaries_returnsTuples() {
        seedTwoProducts();

        transactionTemplate.executeWithoutResult(status -> {
            List<ProductSummary> summaries = productRepository.getProductSummaries();
            assertThat(summaries).hasSize(2);

            ProductSummary s = summaries.stream()
                    .filter(ps -> "TP-X1-2026".equals(ps.sku()))
                    .findFirst()
                    .orElseThrow();

            assertEquals("ThinkPad X1", s.name());
            assertEquals(new BigDecimal("1500.00"), s.price());
            assertEquals("Laptops", s.categoryName());
        });
    }

    @Test
    @DisplayName("save persists a new product and assigns an id")
    void save_persistsAndAssignsId() {
        Long savedId = transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity cat = TestData.category("Electronics");
            s.persist(cat);

            ProductEntity p = TestData.product("New Phone", "PH-1",
                    new BigDecimal("799.00"), cat);
            ProductEntity saved = productRepository.save(p);
            return saved.getId();
        });

        assertThat(savedId).isNotNull();

        transactionTemplate.executeWithoutResult(status ->
                assertThat(productRepository.findById(savedId)).isPresent());
    }

    @Test
    @DisplayName("delete removes the product and cascades to its associations")
    void delete_removesProduct() {
        Long id = seedLaptopWithReview();

        transactionTemplate.executeWithoutResult(status -> productRepository.delete(id));

        transactionTemplate.executeWithoutResult(status -> {
            assertThat(productRepository.findById(id)).isEmpty();
            Long reviewCount = sessionFactory.getCurrentSession()
                    .createSelectionQuery("select count(r) from ReviewEntity r", Long.class)
                    .getSingleResult();
            Long inventoryCount = sessionFactory.getCurrentSession()
                    .createSelectionQuery("select count(i) from WarehouseInventoryEntity i", Long.class)
                    .getSingleResult();
            assertThat(reviewCount).isZero();
            assertThat(inventoryCount).isZero();
        });
    }

    @Test
    @DisplayName("delete is a no-op when the id does not exist")
    void delete_noop_whenMissing() {
        transactionTemplate.executeWithoutResult(status -> productRepository.delete(424242L));
    }

    private Long seedLaptopWithReview() {
        return transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity electronics = TestData.category("Electronics");
            s.persist(electronics);

            ProductEntity laptop = TestData.product("ThinkPad X1", "TP-X1-2026",
                    new BigDecimal("1500.00"), electronics);
            s.persist(laptop);
            ReviewEntity review = TestData.review("Best business laptop!", 5, laptop);
            laptop.addReviewEntity(review);

            return laptop.getId();
        });
    }

    private void seedTwoProducts() {
        transactionTemplate.executeWithoutResult(status -> {
            Session s = sessionFactory.getCurrentSession();

            CategoryEntity electronics = TestData.category("Electronics");
            s.persist(electronics);
            CategoryEntity laptops = TestData.category("Laptops", electronics);
            s.persist(laptops);
            CategoryEntity peripherals = TestData.category("Peripherals", electronics);
            s.persist(peripherals);

            ProductEntity laptop = TestData.product("ThinkPad X1", "TP-X1-2026",
                    new BigDecimal("1500.00"), laptops);
            laptop.getInventoryEntity().setQuantity(60);
            s.persist(laptop);

            ProductEntity mouse = TestData.product("Logi Mouse", "MOUSE-1",
                    new BigDecimal("299.00"), peripherals);
            mouse.getInventoryEntity().setQuantity(5);
            s.persist(mouse);
        });
    }
}
