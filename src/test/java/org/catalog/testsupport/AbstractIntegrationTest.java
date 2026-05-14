package org.catalog.testsupport;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionTemplate;

@SpringJUnitConfig(classes = TestPersistenceConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @BeforeEach
    void resetState() {
        transactionTemplate.executeWithoutResult(status -> {
            Session session = sessionFactory.getCurrentSession();

            session.createMutationQuery("update CategoryEntity c set c.parentEntity = null").executeUpdate();

            session.createMutationQuery("delete from ReviewEntity").executeUpdate();
            session.createMutationQuery("delete from WarehouseInventoryEntity").executeUpdate();
            session.createMutationQuery("delete from ProductEntity").executeUpdate();
            session.createMutationQuery("delete from CategoryEntity").executeUpdate();
        });
        sessionFactory.getCache().evictAllRegions();
        statistics().clear();
    }

    protected Statistics statistics() {
        return sessionFactory.getStatistics();
    }
}
