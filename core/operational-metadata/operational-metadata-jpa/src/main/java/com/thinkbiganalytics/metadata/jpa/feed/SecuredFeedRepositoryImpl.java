package com.thinkbiganalytics.metadata.jpa.feed;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.feed.security.QJpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.QJpaNifiFeedProcessorStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Created by ru186002 on 05/04/2017.
 */
@NoRepositoryBean
public class SecuredFeedRepositoryImpl<T, ID extends Serializable>
    extends QueryDslJpaRepository<T, ID>
    implements SecuredFeedRepository<T, ID>{

    private static final Logger LOG = LoggerFactory.getLogger(SecuredFeedRepositoryImpl.class);

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;
    private final Class<?> springDataRepositoryInterface;

    /**
     * Creates a new {@link SimpleJpaRepository} to manage objects of the given
     * {@link JpaEntityInformation}.
     *
     * @param entityInformation
     * @param entityManager
     */
    public SecuredFeedRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager , Class<?> springDataRepositoryInterface) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.springDataRepositoryInterface = springDataRepositoryInterface;
        LOG.debug("SecuredFeedRepositoryImpl.SecuredFeedRepositoryImpl_0");
    }

    /**
     * Creates a new {@link SimpleJpaRepository} to manage objects of the given
     * domain type.
     *
     * @param domainClass
     * @param em
     */
    public SecuredFeedRepositoryImpl(Class<T> domainClass, EntityManager em) {
        this((JpaEntityInformation<T, ID>) JpaEntityInformationSupport.getEntityInformation(domainClass, em), em, null);
        LOG.debug("SecuredFeedRepositoryImpl.SecuredFeedRepositoryImpl_1");
    }


    @Override
    public void delete(ID id) {
        LOG.debug("SecuredFeedRepositoryImpl.delete");

        super.delete(id);
    }

    @Override
    public void delete(T entity) {
        LOG.debug("SecuredFeedRepositoryImpl.delete");

        super.delete(entity);
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        LOG.debug("SecuredFeedRepositoryImpl.delete");
        super.delete(entities);
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        LOG.debug("SecuredFeedRepositoryImpl.deleteInBatch");
        super.deleteInBatch(entities);
    }

    @Override
    public void deleteAll() {
        LOG.debug("SecuredFeedRepositoryImpl.deleteAll");
        super.deleteAll();
    }

    @Override
    public void deleteAllInBatch() {
        LOG.debug("SecuredFeedRepositoryImpl.deleteAllInBatch");
        super.deleteAllInBatch();
    }

    @Override
    public T findOne(ID id) {
        LOG.debug("SecuredFeedRepositoryImpl.findOne");
        return super.findOne(id);
    }

    @Override
    protected Map<String, Object> getQueryHints() {
        LOG.debug("SecuredFeedRepositoryImpl.getQueryHints");
        return super.getQueryHints();
    }

    @Override
    public T getOne(ID id) {
        LOG.debug("SecuredFeedRepositoryImpl.getOne");
        return super.getOne(id);
    }

    @Override
    public boolean exists(ID id) {
        LOG.debug("SecuredFeedRepositoryImpl.exists");
        return super.exists(id);
    }

    @Override
    public List<T> findAll() {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll();
    }

    @Override
    public List<T> findAll(Iterable<ID> ids) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(ids);
    }

    @Override
    public List<T> findAll(Sort sort) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(pageable);
    }

    @Override
    public T findOne(Specification<T> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.findOne");
        return super.findOne(spec);
    }

    @Override
    public List<T> findAll(Specification<T> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(spec);
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(spec, pageable);
    }

    @Override
    public List<T> findAll(Specification<T> spec, Sort sort) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(spec, sort);
    }

    @Override
    public <S extends T> S findOne(Example<S> example) {
        LOG.debug("SecuredFeedRepositoryImpl.findOne");
        return super.findOne(example);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        LOG.debug("SecuredFeedRepositoryImpl.count");
        return super.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        LOG.debug("SecuredFeedRepositoryImpl.exists");
        return super.exists(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(example, sort);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(example, pageable);
    }

    @Override
    public long count() {
        LOG.debug("SecuredFeedRepositoryImpl.count");
        return super.count();
    }

    @Override
    public long count(Specification<T> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.count");
        return super.count(spec);
    }

    @Override
    public <S extends T> S save(S entity) {
        LOG.debug("SecuredFeedRepositoryImpl.save");
        return super.save(entity);
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        LOG.debug("SecuredFeedRepositoryImpl.saveAndFlush");
        return super.saveAndFlush(entity);
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> entities) {
        LOG.debug("SecuredFeedRepositoryImpl.save");
        return super.save(entities);
    }

    @Override
    public void flush() {
        LOG.debug("SecuredFeedRepositoryImpl.flush");
        super.flush();
    }

    @Override
    protected Page<T> readPage(TypedQuery<T> query, Pageable pageable, Specification<T> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.readPage");
        return super.readPage(query, pageable, spec);
    }

    @Override
    protected <S extends T> Page<S> readPage(TypedQuery<S> query, Class<S> domainClass, Pageable pageable, Specification<S> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.readPage");
        return super.readPage(query, domainClass, pageable, spec);
    }

    @Override
    protected TypedQuery<T> getQuery(Specification<T> spec, Pageable pageable) {
        LOG.debug("SecuredFeedRepositoryImpl.getQuery");
        return super.getQuery(spec, pageable);
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Pageable pageable) {
        LOG.debug("SecuredFeedRepositoryImpl.getQuery");
        return super.getQuery(spec, domainClass, pageable);
    }

    @Override
    protected TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
        LOG.debug("SecuredFeedRepositoryImpl.getQuery");
        return super.getQuery(spec, sort);
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        LOG.debug("SecuredFeedRepositoryImpl.getQuery");

        Specification<S> securedSpec = new Specification<S>() {
            @Override
            public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //and exists (select 1 from JpaFeedOpsAclEntry as x where {root}.id = x.feedId and x.principalName in :#{principal.roleSet})

                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<JpaFeedOpsAclEntry> fromAcl = subquery.from(JpaFeedOpsAclEntry.class);

                subquery.select(fromAcl.get("feedId"));

                SingularAttribute<?, ?> idAttribute = entityInformation.getIdAttribute();
                Predicate rootFeedIdEqualToAclFeedId = cb.equal(root.get(idAttribute.getName()), fromAcl.get("feedId"));

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Collection<String> principalRoles = authentication == null ? new ArrayList<>(0) : new RoleSetExposingSecurityExpressionRoot(authentication).getRoleSet();
                Predicate aclPrincipalNameInRoles = fromAcl.get("principalName").in(principalRoles);

                Predicate feedIdEqualsAndPrincipalInRoles = cb.and(rootFeedIdEqualToAclFeedId, aclPrincipalNameInRoles);
                subquery.where(feedIdEqualsAndPrincipalInRoles);

                Predicate securingPredicate = cb.exists(subquery);

                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, cb);
                    return cb.and(predicate, securingPredicate);
                } else {
                    return securingPredicate;
                }
            }
        };

        return super.getQuery(securedSpec, domainClass, sort);
    }

    @Override
    protected TypedQuery<Long> getCountQuery(Specification<T> spec) {
        LOG.debug("SecuredFeedRepositoryImpl.getCountQuery");
        return super.getCountQuery(spec);
    }

    @Override
    protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
        LOG.debug("SecuredFeedRepositoryImpl.getCountQuery");
        return super.getCountQuery(spec, domainClass);
    }


    @Override
    public List<T> findAll(com.querydsl.core.types.Predicate predicate) {
        LOG.debug("SecuredFeedRepositoryImpl.findAll");
        return super.findAll(predicate);
    }

    @Override
    protected JPQLQuery<?> createQuery(com.querydsl.core.types.Predicate... predicate) {
        LOG.debug("SecuredFeedRepositoryImpl.createQuery");

        //and exists (select 1 from JpaFeedOpsAclEntry as x where {root}.id = x.feedId and x.principalName in :#{principal.roleSet})

        QJpaFeedOpsAclEntry aclEntry = QJpaFeedOpsAclEntry.jpaFeedOpsAclEntry;
        com.querydsl.core.types.Predicate p = null;


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<String> principalRoles = authentication == null ? new ArrayList<>(0) : new RoleSetExposingSecurityExpressionRoot(authentication).getRoleSet();

        QJpaOpsManagerFeed root = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        JPQLQuery<JpaFeedOpsAclEntry> subquery = JPAExpressions.selectFrom(aclEntry).where(aclEntry.feedId.eq(root.id.uuid).and(aclEntry.principalName.in(principalRoles)));

        BooleanExpression exists = subquery.exists();

        List<com.querydsl.core.types.Predicate> predicates = new ArrayList<>();
        predicates.addAll(Arrays.asList(predicate));
        predicates.add(exists);

        return super.createQuery(predicates.toArray(new com.querydsl.core.types.Predicate[] {}));
    }
}