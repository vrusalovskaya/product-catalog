package org.catalog.repositories;

import org.catalog.entities.CategoryEntity;
import org.catalog.testsupport.AbstractIntegrationTest;
import org.catalog.testsupport.TestData;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRepositoryImplTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("getReference returns an uninitialized proxy that resolves to the persisted entity")
    void getReference_returnsLazyProxy() {
        Long id = transactionTemplate.execute(status -> {
            Session s = sessionFactory.getCurrentSession();
            CategoryEntity cat = TestData.category("Electronics");
            s.persist(cat);
            return cat.getId();
        });

        transactionTemplate.executeWithoutResult(status -> {
            CategoryEntity reference = categoryRepository.getReference(id);

            assertThat(reference).isNotNull();
            assertThat(reference.getId()).isEqualTo(id);
            assertThat(Hibernate.isInitialized(reference)).isFalse();

            statistics().clear();

            Hibernate.initialize(reference);
            assertThat(reference.getName()).isEqualTo("Electronics");

            assertThat(statistics().getPrepareStatementCount()).isEqualTo(1);
        });
    }
}
