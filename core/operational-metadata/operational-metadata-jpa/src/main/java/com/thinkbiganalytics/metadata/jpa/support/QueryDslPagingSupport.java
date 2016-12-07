package com.thinkbiganalytics.metadata.jpa.support;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.data.querydsl.QPageRequest;

import java.util.Collections;
import java.util.List;

/**
 * Spring data provides out of the box capability to get paging results via a direct JPA object, but doesn't expose it for JPA queries. This class allows a provider to get page results from a QueryDSL
 * JPAQuery Created by sr186054 on 11/30/16.
 */
public class QueryDslPagingSupport<E> extends QueryDslRepositorySupport {

    public QueryDslPagingSupport(Class<E> clazz) {
        super(clazz);
    }

    protected Page<E> findAll(JPAQuery query, Pageable pageable) {
        if (pageable == null) {
            pageable = new QPageRequest(0, Integer.MAX_VALUE);
        }
        long total = query.clone(super.getEntityManager()).fetchCount();
        JPQLQuery pagedQuery = getQuerydsl().applyPagination(pageable, query);
        List<E> content = total > pageable.getOffset() ? pagedQuery.fetch() : Collections.<E>emptyList();
        return new PageImpl<>(content, pageable, total);
    }

    public Page<E> findAllWithFetch(EntityPathBase<E> path,Predicate predicate, Pageable pageable, QueryDslFetchJoin... joins) {
        if (pageable == null) {
            pageable = new QPageRequest(0, Integer.MAX_VALUE);
        }
        long total = createFetchCountQuery(path, predicate).fetchCount();

        JPQLQuery pagedQuery = getQuerydsl().applyPagination(pageable, createFetchQuery(path,predicate, joins));

        List<E> content = total > pageable.getOffset() ? pagedQuery.fetch() : Collections.<E>emptyList();
        return new PageImpl<>(content, pageable, total);
    }


    private JPQLQuery createFetchCountQuery(EntityPathBase<E> path,Predicate predicate) {
        JPQLQuery query = from(path);
        query.where(predicate);
        return query;
    }

    private JPQLQuery createFetchQuery(EntityPathBase<E> path,Predicate predicate, QueryDslFetchJoin... joins) {
        JPQLQuery query = from(path);
        for(QueryDslFetchJoin joinDescriptor: joins) {
            join(joinDescriptor, query);
        }
        query.where(predicate);
        return query;
    }

    private JPQLQuery join(QueryDslFetchJoin join, JPQLQuery query) {
        switch(join.type) {
            case INNER:
                query.innerJoin(join.joinPath).fetchJoin();
                break;
            case JOIN:
                query.join(join.joinPath).fetchJoin();
                break;
            case LEFT:
                query.leftJoin(join.joinPath).fetchJoin();
                break;
            case RIGHT:
                query.rightJoin(join.joinPath).fetchJoin();
                break;
            case INNER_ALIAS:
                query.innerJoin(join.joinPath,join.alias).fetchJoin();
                break;
            case JOIN_ALIAS:
                query.join(join.joinPath,join.alias).fetchJoin();
                break;
            case LEFT_ALIAS:
                query.leftJoin(join.joinPath,join.alias).fetchJoin();
                break;
            case RIGHT_ALIAS:
                query.rightJoin(join.joinPath,join.alias).fetchJoin();
                break;
        }
        return query;
    }

}