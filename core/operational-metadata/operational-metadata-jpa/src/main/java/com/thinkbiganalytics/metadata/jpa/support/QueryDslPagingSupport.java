package com.thinkbiganalytics.metadata.jpa.support;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
 * JPAQuery
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

    public Page<E> findAllWithFetch(EntityPathBase<E> path, Predicate predicate, Pageable pageable, QueryDslFetchJoin... joins) {
        if (pageable == null) {
            pageable = new QPageRequest(0, Integer.MAX_VALUE);
        }
        long total = createFetchCountQuery(path, predicate).fetchCount();

        JPQLQuery pagedQuery = getQuerydsl().applyPagination(pageable, createFetchQuery(path, predicate, joins));

        List<E> content = total > pageable.getOffset() ? pagedQuery.fetch() : Collections.<E>emptyList();
        return new PageImpl<>(content, pageable, total);
    }


    private JPQLQuery createFetchCountQuery(EntityPathBase<E> path, Predicate predicate) {
        JPQLQuery query = from(path);
        query.where(predicate);
        return query;
    }

    private JPQLQuery createFetchQuery(EntityPathBase<E> path, Predicate predicate, QueryDslFetchJoin... joins) {
        JPQLQuery query = from(path);
        for (QueryDslFetchJoin joinDescriptor : joins) {
            join(joinDescriptor, query);
        }
        query.where(predicate);
        return query;
    }

    private JPQLQuery join(QueryDslFetchJoin join, JPQLQuery query) {
        switch (join.type) {
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
                query.innerJoin(join.joinPath, join.alias).fetchJoin();
                break;
            case JOIN_ALIAS:
                query.join(join.joinPath, join.alias).fetchJoin();
                break;
            case LEFT_ALIAS:
                query.leftJoin(join.joinPath, join.alias).fetchJoin();
                break;
            case RIGHT_ALIAS:
                query.rightJoin(join.joinPath, join.alias).fetchJoin();
                break;
        }
        return query;
    }

}
