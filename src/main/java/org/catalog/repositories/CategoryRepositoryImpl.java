package org.catalog.repositories;

import lombok.RequiredArgsConstructor;
import org.catalog.entities.CategoryEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final SessionFactory sessionFactory;

    @Override
    public CategoryEntity getReference(Long id) {
        Session session = sessionFactory.getCurrentSession();

        return session.getReference(CategoryEntity.class, id);
    }
}
