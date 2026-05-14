package org.catalog.repositories;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.catalog.entities.ProductEntity_;
import org.catalog.entities.ReviewEntity;
import org.catalog.entities.ReviewEntity_;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final SessionFactory sessionFactory;

    @Override
    public ReviewEntity save(ReviewEntity review) {
        Session session = sessionFactory.getCurrentSession();

        session.persist(review);
        return review;
    }

    @Override
    public Optional<ReviewEntity> findByIdAndProductId(Long reviewId, Long productId) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ReviewEntity> cq = cb.createQuery(ReviewEntity.class);
        Root<ReviewEntity> review = cq.from(ReviewEntity.class);

        cq.where(
                cb.and(
                        cb.equal(review.get(ReviewEntity_.id), reviewId),
                        cb.equal(review.get(ReviewEntity_.productEntity).get(ProductEntity_.id), productId)
                )
        );

        return session.createQuery(cq).uniqueResultOptional();
    }

    @Override
    public void delete(Long productId, Long reviewId) {
        Session session = sessionFactory.getCurrentSession();

        Optional<ReviewEntity> entity = findByIdAndProductId(reviewId, productId);
        entity.ifPresent(session::remove);
    }
}
