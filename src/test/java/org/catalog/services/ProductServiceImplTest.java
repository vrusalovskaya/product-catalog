package org.catalog.services;

import jakarta.persistence.EntityNotFoundException;
import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.records.Category;
import org.catalog.records.Product;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.catalog.records.Review;
import org.catalog.records.WarehouseInventory;
import org.catalog.testsupport.AbstractIntegrationTest;
import org.catalog.testsupport.TestData;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("findById returns the product DTO when present")
    void findById_returnsProduct() {
        Long id = seedLaptopWithReview();

        Product product = productService.findById(id);

        assertThat(product.id()).isEqualTo(id);
        assertThat(product.name()).isEqualTo("ThinkPad X1");
        assertThat(product.sku()).isEqualTo("TP-X1-2026");
        assertThat(product.price()).isEqualByComparingTo("1500.00");
        assertThat(product.category().name()).isEqualTo("Electronics");
        assertThat(product.inventory().quantity()).isEqualTo(10);
        assertThat(product.reviews()).hasSize(1);
    }

    @Test
    @DisplayName("findById throws EntityNotFoundException for missing ids")
    void findById_throws_whenMissing() {
        assertThatThrownBy(() -> productService.findById(424242L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("findBySku returns product DTO using the natural id")
    void findBySku_returnsProduct() {
        seedLaptopWithReview();

        Product product = productService.findBySku("TP-X1-2026");

        assertThat(product).isNotNull();
        assertThat(product.name()).isEqualTo("ThinkPad X1");
    }

    @Test
    @DisplayName("findBySku throws EntityNotFoundException for unknown sku")
    void findBySku_throws_whenMissing() {
        assertThatThrownBy(() -> productService.findBySku("nope"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("search delegates to the repository and maps to DTOs")
    void search_returnsMappedDtos() {
        seedTwoProducts();

        List<Product> results = productService.search(
                new ProductSearchCriteria("Logi", null, null, null, null, null));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().sku()).isEqualTo("MOUSE-1");
    }

    @Test
    @DisplayName("findTopRated maps repository entities into DTOs")
    void findTopRated_mapsEntities() {
        seedTwoProducts();

        List<Product> top = productService.findTopRated(10);

        assertThat(top).extracting(Product::sku)
                .containsExactlyInAnyOrder("TP-X1-2026", "MOUSE-1");
    }

    @Test
    @DisplayName("getProductSummaries returns flat ProductSummary records")
    void getProductSummaries_returnsRecords() {
        seedTwoProducts();

        List<ProductSummary> summaries = productService.getProductSummaries();

        assertThat(summaries).hasSize(2);
        ProductSummary laptop = summaries.stream()
                .filter(s -> "TP-X1-2026".equals(s.sku()))
                .findFirst()
                .orElseThrow();
        assertThat(laptop.name()).isEqualTo("ThinkPad X1");
        assertThat(laptop.categoryName()).isEqualTo("Laptops");
    }

    @Test
    @DisplayName("save persists a Product passed as a record")
    void save_persistsProduct() {
        Long categoryId = transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity electronics = TestData.category("Electronics");
            s.persist(electronics);
            return electronics.getId();
        });

        Product newProduct = new Product(
                null,
                "Pixel 9",
                new BigDecimal("999.00"),
                "PIX-9",
                new Category(categoryId, "Electronics", null, null),
                new WarehouseInventory(7, "Warehouse B"),
                List.of()
        );

        Product saved = productService.save(newProduct);

        assertThat(saved.id()).isNotNull();

        transactionTemplate.executeWithoutResult(status -> {
            ProductEntity loaded = sessionFactory.getCurrentSession()
                    .find(ProductEntity.class, saved.id());
            assertThat(loaded).isNotNull();
            assertThat(loaded.getSku()).isEqualTo("PIX-9");
        });
    }

    @Test
    @DisplayName("update mutates name, price and adds new reviews")
    void update_changesScalarFieldsAndAddsReview() {
        Long id = seedLaptopWithReview();

        Product fetched = productService.findById(id);

        Product mutated = new Product(
                fetched.id(),
                "ThinkPad X1 Carbon",
                new BigDecimal("1700.00"),
                fetched.sku(),
                fetched.category(),
                fetched.inventory(),
                List.of(
                        fetched.reviews().getFirst(),
                        new Review(null, "Solid keyboard", 4)
                )
        );

        Product updated = productService.update(mutated);

        assertThat(updated.name()).isEqualTo("ThinkPad X1 Carbon");
        assertThat(updated.price()).isEqualByComparingTo("1700.00");
        assertThat(updated.reviews()).hasSize(2);

        Product reloaded = productService.findById(id);
        assertThat(reloaded.name()).isEqualTo("ThinkPad X1 Carbon");
        assertThat(reloaded.reviews()).hasSize(2);
    }

    @Test
    @DisplayName("update removes reviews that are no longer present in the input")
    void update_removesMissingReviews() {
        Long id = seedLaptopWithReview();
        Product fetched = productService.findById(id);
        assertThat(fetched.reviews()).hasSize(1);

        Product withoutReviews = new Product(
                fetched.id(),
                fetched.name(),
                fetched.price(),
                fetched.sku(),
                fetched.category(),
                fetched.inventory(),
                List.of()
        );

        productService.update(withoutReviews);

        Product reloaded = productService.findById(id);
        assertThat(reloaded.reviews()).isEmpty();
    }

    @Test
    @DisplayName("update throws EntityNotFoundException when product does not exist")
    void update_throws_whenMissing() {
        Product ghost = new Product(
                404L, "ghost", new BigDecimal("1.00"), "G-1",
                new Category(1L, "x", null, null),
                new WarehouseInventory(0, "x"),
                List.of()
        );

        assertThatThrownBy(() -> productService.update(ghost))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("delete removes the product")
    void delete_removesProduct() {
        Long id = seedLaptopWithReview();

        productService.delete(id);

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Long seedLaptopWithReview() {
        return transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity electronics = TestData.category("Electronics");
            s.persist(electronics);
            ProductEntity laptop = TestData.product("ThinkPad X1", "TP-X1-2026",
                    new BigDecimal("1500.00"), electronics);
            laptop.getReviewEntities().add(TestData.review("Best business laptop!", 5));
            s.persist(laptop);
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
            laptop.getReviewEntities().add(TestData.review("Loved it", 5));
            laptop.getInventoryEntity().setQuantity(60);
            s.persist(laptop);

            ProductEntity mouse = TestData.product("Logi Mouse", "MOUSE-1",
                    new BigDecimal("299.00"), peripherals);
            mouse.getReviewEntities().add(TestData.review("Average", 3));
            mouse.getInventoryEntity().setQuantity(5);
            s.persist(mouse);
        });
    }
}
