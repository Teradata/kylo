package com.thinkbiganalytics.metadata.jpa.feed;

/*-
 * #%L
 * kylo-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.querydsl.jpa.JPQLQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.SingularAttribute;

/**
 * This repository delegates query augmentation to an instance of QueryAugmentor
 */
@NoRepositoryBean
public class AugmentableQueryRepositoryImpl<T, ID extends Serializable>
    extends QueryDslJpaRepository<T, ID>
    implements AugmentableQueryRepository<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableQueryRepositoryImpl.class);
    private static final String QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET = "Query by Example API not implemented yet";

    protected final EntityManager entityManager;
    protected final JpaEntityInformation<T, ID> entityInformation;
    protected QueryAugmentor augmentor;

    public AugmentableQueryRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager em) {
        this(entityInformation, em, null);
    }

    public AugmentableQueryRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager em, QueryAugmentor augmentor) {
        super(entityInformation, em);
        this.entityInformation = entityInformation;
        this.entityManager = em;
        this.augmentor = augmentor;
    }

    @Override
    public long count() {
        LOG.debug("AugmentableQueryRepositoryImpl.count");
        TypedQuery<Long> query = this.getCountQuery(null, getDomainClass());
        return query.getSingleResult();
    }

    @Override
    protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
        LOG.debug("AugmentableQueryRepositoryImpl.getCountQuery");
        if (augmentor != null) {
            CriteriaQuery<Long> query = augmentor.getCountQuery(entityManager, entityInformation, spec, domainClass);
            return entityManager.createQuery(query);
        } else {
            return super.getCountQuery(spec, domainClass);
        }
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.getQuery");
        return super.getQuery(augmentor != null ? augmentor.augment(spec, domainClass, entityInformation) : spec, domainClass, sort);
    }

    @Override
    protected JPQLQuery<?> createQuery(com.querydsl.core.types.Predicate... predicate) {
        LOG.debug("AugmentableQueryRepositoryImpl.createQuery");

        return super.createQuery(augmentor != null ? augmentor.augment(predicate).toArray(new com.querydsl.core.types.Predicate[]{}) : predicate);
    }

    @Override
    public T findOne(ID id) {
        LOG.debug("AugmentableQueryRepositoryImpl.findOne");

        Specification<T> spec = (root, query, cb) -> {
            SingularAttribute<?, ?> idAttribute = entityInformation.getIdAttribute();
            return cb.equal(root.get(idAttribute.getName()), id);
        };

        return findOne(spec);
    }

    @Override
    public T getOne(ID id) {
        LOG.debug("AugmentableQueryRepositoryImpl.getOne");
        return findOne(id);
    }

    @Override
    public boolean exists(ID id) {
        LOG.debug("AugmentableQueryRepositoryImpl.exists");
        return findOne(id) != null;
    }


    @Override
    public <S extends T> S findOne(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.findOne example");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.count example");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.exists example");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll example ");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll example sort");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll example pageable");
        throw new IllegalStateException(QUERY_BY_EXAMPLE_API_NOT_IMPLEMENTED_YET);
    }

    public void setAugmentor(QueryAugmentor augmentor) {
        this.augmentor = augmentor;
    }
}