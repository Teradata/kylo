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

import com.querydsl.core.types.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Type which can participate in query augmentation with AugmentableRepository
 */
public interface QueryAugmentor {

    <S, T, ID extends Serializable> Specification<S> augment(Specification<S> spec, Class<S> domainClass,
                                 JpaEntityInformation<T, ID> entityInformation);

    List<Predicate> augment(Predicate[] predicate);

    <S, T, ID extends Serializable> CriteriaQuery<Long> getCountQuery(EntityManager entityManager, JpaEntityInformation<T, ID> entityInformation, Specification<S> spec, Class<S> domainClass);
}
