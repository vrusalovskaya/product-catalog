package org.catalog.repositories;

import lombok.RequiredArgsConstructor;
import org.catalog.entities.WarehouseInventoryEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WarehouseInventoryRepositoryImpl implements WarehouseInventoryRepository {

    private final SessionFactory sessionFactory;

    @Override
    public WarehouseInventoryEntity getReference(Long id) {
        Session session = sessionFactory.getCurrentSession();

        return session.getReference(WarehouseInventoryEntity.class, id);
    }
}
