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
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

/**
 * This repository delegates query augmentation to an instance of QueryAugmentor
 */
@NoRepositoryBean
public class AugmentableQueryRepositoryImpl<T, ID extends Serializable>
    extends QueryDslJpaRepository<T, ID>
    implements AugmentableQueryRepository<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableQueryRepositoryImpl.class);

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;
    private final Class<?> springDataRepositoryInterface;
    private final QueryAugmentor augmentor;
    private final PersistenceProvider provider;

    /**
     * Creates a new {@link SimpleJpaRepository} to manage objects of the given
     * {@link JpaEntityInformation}.
     *
     * @param entityInformation
     * @param entityManager
     */
    public AugmentableQueryRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager , Class<?> springDataRepositoryInterface) {
        this(entityInformation, entityManager, springDataRepositoryInterface, null);
        LOG.debug("AugmentableQueryRepositoryImpl.SecuredFeedRepositoryImpl_0");
    }

    /**
     * Creates a new {@link SimpleJpaRepository} to manage objects of the given
     * domain type.
     *
     * @param domainClass
     * @param em
     */
    public AugmentableQueryRepositoryImpl(Class<T> domainClass, EntityManager em) {
        this((JpaEntityInformation<T, ID>) JpaEntityInformationSupport.getEntityInformation(domainClass, em), em, null);
        LOG.debug("AugmentableQueryRepositoryImpl.SecuredFeedRepositoryImpl_1");
    }

    public AugmentableQueryRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager em, Class<?> repositoryInterface, QueryAugmentor augmentor) {
        super(entityInformation, em);
        this.entityInformation = entityInformation;
        this.entityManager = em;
        this.springDataRepositoryInterface = repositoryInterface;
        this.augmentor = augmentor;
        this.provider = PersistenceProvider.fromEntityManager(entityManager);
    }

    @Override
    public long count() {
        LOG.debug("AugmentableQueryRepositoryImpl.count");
        CriteriaQuery<Long> query = augmentor.getCountQuery(entityManager, entityInformation, getDomainClass());
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.getQuery");
        return super.getQuery(augmentor.augment(spec, domainClass, entityInformation), domainClass, sort);
    }

    @Override
    protected JPQLQuery<?> createQuery(com.querydsl.core.types.Predicate... predicate) {
        LOG.debug("AugmentableQueryRepositoryImpl.createQuery");

        return super.createQuery(augmentor.augment(predicate).toArray(new com.querydsl.core.types.Predicate[] {}));
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
    public void delete(ID id) {
        LOG.debug("AugmentableQueryRepositoryImpl.delete");

        super.delete(id);
    }

    @Override
    public void delete(T entity) {
        LOG.debug("AugmentableQueryRepositoryImpl.delete");

        super.delete(entity);
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        LOG.debug("AugmentableQueryRepositoryImpl.delete");
        super.delete(entities);
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        LOG.debug("AugmentableQueryRepositoryImpl.deleteInBatch");
        super.deleteInBatch(entities);
    }

    @Override
    public void deleteAll() {
        LOG.debug("AugmentableQueryRepositoryImpl.deleteAll");
        super.deleteAll();
    }

    @Override
    public void deleteAllInBatch() {
        LOG.debug("AugmentableQueryRepositoryImpl.deleteAllInBatch");
        super.deleteAllInBatch();
    }




    @Override
    public List<T> findAll(Specification<T> spec) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(spec);
    }


    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(spec, pageable);
    }

    @Override
    public List<T> findAll(Specification<T> spec, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(spec, sort);
    }

    @Override
    public <S extends T> S findOne(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.findOne");
        return super.findOne(example);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.count");
        return super.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.exists");
        return super.exists(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(example, sort);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        LOG.debug("AugmentableQueryRepositoryImpl.findAll");
        return super.findAll(example, pageable);
    }

    @Override
    public long count(Specification<T> spec) {
        LOG.debug("AugmentableQueryRepositoryImpl.count");
        return super.count(spec);
    }

    @Override
    public <S extends T> S save(S entity) {
        LOG.debug("AugmentableQueryRepositoryImpl.save");
        return super.save(entity);
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        LOG.debug("AugmentableQueryRepositoryImpl.saveAndFlush");
        return super.saveAndFlush(entity);
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> entities) {
        LOG.debug("AugmentableQueryRepositoryImpl.save");
        return super.save(entities);
    }

    @Override
    public void flush() {
        LOG.debug("AugmentableQueryRepositoryImpl.flush");
        super.flush();
    }

    @Override
    protected Page<T> readPage(TypedQuery<T> query, Pageable pageable, Specification<T> spec) {
        LOG.debug("AugmentableQueryRepositoryImpl.readPage");
        return super.readPage(query, pageable, spec);
    }

    @Override
    protected <S extends T> Page<S> readPage(TypedQuery<S> query, Class<S> domainClass, Pageable pageable, Specification<S> spec) {
        LOG.debug("AugmentableQueryRepositoryImpl.readPage");
        return super.readPage(query, domainClass, pageable, spec);
    }

    @Override
    protected TypedQuery<T> getQuery(Specification<T> spec, Pageable pageable) {
        LOG.debug("AugmentableQueryRepositoryImpl.getQuery");
        return super.getQuery(spec, pageable);
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Pageable pageable) {
        LOG.debug("AugmentableQueryRepositoryImpl.getQuery");
        return super.getQuery(spec, domainClass, pageable);
    }

    @Override
    protected TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
        LOG.debug("AugmentableQueryRepositoryImpl.getQuery");
        return super.getQuery(spec, sort);
    }

    @Override
    protected TypedQuery<Long> getCountQuery(Specification<T> spec) {
        LOG.debug("AugmentableQueryRepositoryImpl.getCountQuery");
        return super.getCountQuery(spec);
    }

    @Override
    protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
        LOG.debug("AugmentableQueryRepositoryImpl.getCountQuery");
        return super.getCountQuery(spec, domainClass);
    }


}