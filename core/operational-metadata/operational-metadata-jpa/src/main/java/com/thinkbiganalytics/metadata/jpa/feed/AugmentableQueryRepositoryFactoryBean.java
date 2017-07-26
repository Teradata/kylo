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

import com.thinkbiganalytics.security.AccessController;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Creates AugmentableQueryRepositoryFactory
 */
public class AugmentableQueryRepositoryFactoryBean<R extends JpaRepository<T, I>, T,
    I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {


    @Inject
    AccessController accessController;

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new AugmentableQueryRepositoryFactory(em, accessController);
    }

}
