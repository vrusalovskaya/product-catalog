package org.catalog.repositories;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.catalog.entities.*;
import org.catalog.records.ProductSearchCriteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final SessionFactory sessionFactory;

    @Override
    public ProductEntity findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(cb.equal(root.get(ProductEntity_.id), id));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        List<Predicate> predicates = parseCriteria(criteria, cb, root);

        cq.where(predicates.toArray(new Predicate[0]));

        return session.createSelectionQuery(cq).getResultList();
    }

    @Override
    public List<ProductEntity> findTopRated(int limit) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        Join<ProductEntity, ReviewEntity> reviews = product.join(ProductEntity_.reviewEntities, JoinType.LEFT);

        cq.select(product)
                .groupBy(product.get(ProductEntity_.id))
                .orderBy(cb.desc(cb.avg(reviews.get(ReviewEntity_.rating))));

        return session.createSelectionQuery(cq)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Tuple> getProductSummaries() {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        Join<ProductEntity, CategoryEntity> category = product.join(ProductEntity_.categoryEntity);

        cq.multiselect(
                product.get(ProductEntity_.id).alias("id"),
                product.get(ProductEntity_.name).alias("productName"),
                product.get(ProductEntity_.price).alias("productPrice"),
                product.get(ProductEntity_.sku).alias("sku"),
                category.get(CategoryEntity_.name).alias("categoryName")
        );

        return session.createSelectionQuery(cq)
                .setCacheable(true)
                .getResultList();
    }

    private static List<Predicate> parseCriteria(ProductSearchCriteria criteria, CriteriaBuilder cb, Root<ProductEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.name() != null) {
            predicates.add(cb.equal(root.get(ProductEntity_.name), criteria.name()));
        }

        if (criteria.minPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(ProductEntity_.price), criteria.minPrice()));
        }

        if (criteria.maxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(ProductEntity_.price), criteria.maxPrice()));
        }

        if (criteria.sku() != null) {
            predicates.add(cb.equal(root.get(ProductEntity_.sku), criteria.sku()));
        }

        if (criteria.categoryName() != null) {
            Join<ProductEntity, CategoryEntity> categoryJoin = root.join(ProductEntity_.categoryEntity);
            predicates.add(cb.equal(categoryJoin.get(CategoryEntity_.name), criteria.categoryName()));
        }

        if (criteria.minStock() != null) {
            Join<ProductEntity, WarehouseInventoryEntity> inventoryJoin = root.join(ProductEntity_.inventoryEntity);
            predicates.add(cb.greaterThanOrEqualTo(inventoryJoin.get(WarehouseInventoryEntity_.quantity), criteria.minStock()));
        }

        return predicates;
    }
}
