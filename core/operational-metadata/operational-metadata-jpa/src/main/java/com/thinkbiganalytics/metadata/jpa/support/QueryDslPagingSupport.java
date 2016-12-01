package com.thinkbiganalytics.metadata.jpa.support;

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

}