package org.catalog.cache;

import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.records.Product;
import org.catalog.repositories.ProductRepository;
import org.catalog.services.ProductService;
import org.catalog.testsupport.AbstractIntegrationTest;
import org.catalog.testsupport.TestData;
import org.hibernate.Session;
import org.hibernate.stat.CacheRegionStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SecondLevelCacheTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("findById hits the product L2 cache on the second transaction")
    void findById_populatesAndReusesProductCache() {
        Long id = seedLaptop();
        sessionFactory.getCache().evictAllRegions();
        statistics().clear();

        transactionTemplate.executeWithoutResult(s -> productRepository.findById(id));

        CacheRegionStatistics afterFirst = statistics().getCacheRegionStatistics("product-cache");
        assert afterFirst != null;
        long hitsBefore = afterFirst.getHitCount();
        assertThat(afterFirst.getMissCount()).isGreaterThanOrEqualTo(1);
        assertThat(afterFirst.getPutCount()).isGreaterThanOrEqualTo(1);
        assertEquals(0, hitsBefore);

        transactionTemplate.executeWithoutResult(s -> productRepository.findById(id));

        CacheRegionStatistics afterSecond = statistics().getCacheRegionStatistics("product-cache");
        assert afterSecond != null;
        assertThat(afterSecond.getHitCount()).isGreaterThan(hitsBefore);
    }

    @Test
    @DisplayName("findBySku hits the natural-id L2 cache on the second transaction")
    void findBySku_populatesAndReusesNaturalIdCache() {
        seedLaptop();
        sessionFactory.getCache().evictAllRegions();
        statistics().clear();

        transactionTemplate.executeWithoutResult(s -> productRepository.findBySku("TP-X1-2026"));

        CacheRegionStatistics naturalIdStats =
                statistics().getCacheRegionStatistics("product-natural-id-cache");
        assert naturalIdStats != null;
        assertThat(naturalIdStats.getMissCount()).isGreaterThanOrEqualTo(1);
        assertThat(naturalIdStats.getPutCount()).isGreaterThanOrEqualTo(1);

        long hitsBefore = naturalIdStats.getHitCount();

        transactionTemplate.executeWithoutResult(s -> productRepository.findBySku("TP-X1-2026"));

        CacheRegionStatistics afterSecond =
                statistics().getCacheRegionStatistics("product-natural-id-cache");
        assert afterSecond != null;
        assertThat(afterSecond.getHitCount()).isGreaterThan(hitsBefore);
    }

    @Test
    @DisplayName("Loading a product warms the category L2 cache via the lazy association")
    void loadingProduct_populatesCategoryCache() {
        Long productId = seedLaptop();
        sessionFactory.getCache().evictAllRegions();
        statistics().clear();

        transactionTemplate.executeWithoutResult(s -> {
            Product p = productService.findById(productId);
            assertThat(p.category().name()).isEqualTo("Electronics");
        });

        CacheRegionStatistics categoryStats =
                statistics().getCacheRegionStatistics("category-cache");
        assert categoryStats != null;
        assertThat(categoryStats.getPutCount()).isGreaterThanOrEqualTo(1);

        long hitsAfterFirst = categoryStats.getHitCount();

        transactionTemplate.executeWithoutResult(s -> {
            Product p = productService.findById(productId);
            assertThat(p.category().name()).isEqualTo("Electronics");
        });

        CacheRegionStatistics afterSecond =
                statistics().getCacheRegionStatistics("category-cache");
        assert afterSecond != null;
        assertThat(afterSecond.getHitCount()).isGreaterThan(hitsAfterFirst);
    }

    @Test
    @DisplayName("getProductSummaries reuses the query cache on the second invocation")
    void getProductSummaries_reusesQueryCache() {
        seedLaptop();
        sessionFactory.getCache().evictAllRegions();
        statistics().clear();

        transactionTemplate.executeWithoutResult(s -> productRepository.getProductSummaries());
        long executionsAfterFirst = statistics().getQueryExecutionCount();

        assertThat(statistics().getQueryCacheMissCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics().getQueryCachePutCount()).isGreaterThanOrEqualTo(1);

        transactionTemplate.executeWithoutResult(s -> productRepository.getProductSummaries());

        assertThat(statistics().getQueryCacheHitCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics().getQueryExecutionCount()).isEqualTo(executionsAfterFirst);
    }

    @Test
    @DisplayName("Cache eviction forces a fresh DB load")
    void evictingCache_forcesReload() {
        Long id = seedLaptop();

        transactionTemplate.executeWithoutResult(s -> productRepository.findById(id));

        sessionFactory.getCache().evictEntityData(ProductEntity.class, id);
        statistics().clear();

        transactionTemplate.executeWithoutResult(s -> productRepository.findById(id));

        CacheRegionStatistics productStats =
                statistics().getCacheRegionStatistics("product-cache");
        assert productStats != null;
        assertThat(productStats.getMissCount()).isGreaterThanOrEqualTo(1);
    }

    private Long seedLaptop() {
        return transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity electronics = TestData.category("Electronics");
            s.persist(electronics);
            ProductEntity laptop = TestData.product("ThinkPad X1", "TP-X1-2026",
                    new BigDecimal("1500.00"), electronics);
            s.persist(laptop);
            return laptop.getId();
        });
    }
}
