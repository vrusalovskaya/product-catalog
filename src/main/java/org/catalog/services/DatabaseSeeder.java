package org.catalog.services;

import lombok.RequiredArgsConstructor;
import org.catalog.entities.CategoryEntity;
import org.catalog.entities.ProductEntity;
import org.catalog.entities.ReviewEntity;
import org.catalog.entities.WarehouseInventoryEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final SessionFactory sessionFactory;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void seed() {
        Session session = sessionFactory.getCurrentSession();

        Long productCount = session.createSelectionQuery("select count(p) from ProductEntity p", Long.class)
                .getSingleResult();

        if (productCount == 0) {
            System.out.println("Database is empty. Starting seeding process...");
            createData(session);
            System.out.println("Seeding completed successfully.");
        }
    }

    private void createData(Session session) {
        CategoryEntity electronics = new CategoryEntity();
        electronics.setName("Electronics");
        session.persist(electronics);

        CategoryEntity laptops = new CategoryEntity();
        laptops.setName("Laptops");
        laptops.setParentEntity(electronics);
        session.persist(laptops);

        ProductEntity laptop = new ProductEntity();
        laptop.setName("ThinkPad X1");
        laptop.setPrice(new BigDecimal("1500.00"));
        laptop.setSku("TP-X1-2026");
        laptop.setCategoryEntity(laptops);

        WarehouseInventoryEntity inventory = new WarehouseInventoryEntity();
        inventory.setQuantity(50);
        inventory.setLocation("Warehouse A");

        inventory.setProductEntity(laptop);
        laptop.setInventoryEntity(inventory);

        ReviewEntity rev1 = new ReviewEntity();
        rev1.setContent("Best business laptop!");
        rev1.setRating(5);

        laptop.getReviewEntities().add(rev1);

        session.persist(laptop);
    }
}
