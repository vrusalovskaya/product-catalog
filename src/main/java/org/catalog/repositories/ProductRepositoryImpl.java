package org.catalog.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.catalog.entities.*;
import org.catalog.records.ProductSearchCriteria;
import org.catalog.records.ProductSummary;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@AllArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final SessionFactory sessionFactory;

    @Override
    public Optional<ProductEntity> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        return Optional.ofNullable(session.find(ProductEntity.class, id));
    }

    @Override
    public Optional<ProductEntity> findWithFetch(Long id) {
        Session session = sessionFactory.getCurrentSession();
        EntityGraph<ProductEntity> graph = session.createEntityGraph(ProductEntity.class);
        graph.addAttributeNodes("categoryEntity", "inventoryEntity", "reviewEntities");

        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", graph);

        ProductEntity product = session.find(ProductEntity.class, id, hints);
        return Optional.ofNullable(product);
    }

    @Override
    public Optional<ProductEntity> findBySku(String sku) {
        return Optional.ofNullable(sessionFactory.getCurrentSession()
                .byNaturalId(ProductEntity.class)
                .using("sku", sku)
                .load());
    }

    @Override
    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        List<Predicate> predicates = parseCriteria(criteria, cb, root);

        cq.where(predicates.toArray(new Predicate[0])).distinct(true);

        return session.createSelectionQuery(cq).getResultList();
    }

    @Override
    public List<ProductEntity> findTopRated(int limit) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        Join<ProductEntity, ReviewEntity> reviews = product.join(ProductEntity_.reviewEntities, JoinType.LEFT);
        Expression<Double> avgRating = cb.coalesce(
                cb.avg(reviews.get(ReviewEntity_.rating)),
                0.0
        );

        cq.select(product)
                .groupBy(product.get(ProductEntity_.id),
                        product.get(ProductEntity_.name),
                        product.get(ProductEntity_.price),
                        product.get(ProductEntity_.sku))
                .orderBy(cb.desc(avgRating));
        return session.createSelectionQuery(cq)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<ProductSummary> getProductSummaries() {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ProductSummary> cq = cb.createQuery(ProductSummary.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        Join<ProductEntity, CategoryEntity> category = product.join(ProductEntity_.categoryEntity);

        cq.select(cb.construct(
                ProductSummary.class,
                product.get(ProductEntity_.id),
                product.get(ProductEntity_.name),
                product.get(ProductEntity_.price),
                product.get(ProductEntity_.sku),
                category.get(CategoryEntity_.name)
        ));

        return session.createQuery(cq)
                .setCacheable(true)
                .getResultList();
    }

    @Override
    public ProductEntity save(ProductEntity entity) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        Session session = sessionFactory.getCurrentSession();

        ProductEntity loadedEntity = session.find(ProductEntity.class, id);
        if (loadedEntity != null) {
            session.remove(loadedEntity);
        }
    }

    private static List<Predicate> parseCriteria(ProductSearchCriteria criteria, CriteriaBuilder cb, Root<ProductEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.name() != null) {
            predicates.add(cb.like(
                    cb.lower(root.get(ProductEntity_.name)),
                    "%" + criteria.name().toLowerCase() + "%"));
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
