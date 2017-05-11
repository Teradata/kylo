package com.thinkbiganalytics.metadata.jpa.feed;

import com.querydsl.core.types.dsl.ComparablePath;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 */
public class LatestFeedJobExectionSecuringRepository extends AugmentableQueryRepositoryImpl {

    public LatestFeedJobExectionSecuringRepository(JpaEntityInformation entityInformation, EntityManager em) {
        super(entityInformation, em, new FeedAclIndexQueryAugmentor() {
            @Override
            protected <S, T, ID extends Serializable> Path<Object> getFeedId(JpaEntityInformation<T, ID> entityInformation, Root<S> root) {
                return root.get("feed").get("id");
            }

            @Override
            protected ComparablePath<UUID> getFeedId() {
                throw new IllegalStateException("Dsl Query API not supported for this repository");
            }

            @Override
            protected QOpsManagerFeedId getOpsManagerFeedId() {
                throw new IllegalStateException("Dsl Query API not supported for this repository");
            }
        });
    }
}
