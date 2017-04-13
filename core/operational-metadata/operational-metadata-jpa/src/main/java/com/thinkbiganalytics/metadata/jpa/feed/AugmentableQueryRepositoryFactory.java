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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * Factory which knows how to create AugmentableRepositories with QueryAugmentors
 */
class AugmentableQueryRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableQueryRepositoryFactory.class);

    private final EntityManager em;
    private final AutowireCapableBeanFactory beanFactory;

    AugmentableQueryRepositoryFactory(EntityManager em, AutowireCapableBeanFactory beanFactory) {
        super(em);
        this.em = em;
        this.beanFactory = beanFactory;
        LOG.debug("AugmentableQueryRepositoryFactory.AugmentableQueryRepositoryFactory");
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        LOG.debug("AugmentableQueryRepositoryFactory.getTargetRepository");

        Class<?> repositoryInterface = information.getRepositoryInterface();

        if (isAugmentableRepository(repositoryInterface)) {
            Class<?> domainType = information.getDomainType();
            LOG.debug("Creating AugmentableQueryRepositoryImpl for repo interface {} and domain class {}", repositoryInterface, domainType);

            QueryAugmentorType annotation = AnnotationUtils.findAnnotation(repositoryInterface, QueryAugmentorType.class);
            QueryAugmentor augmentor = null;
            if (annotation != null) {
                Class<? extends QueryAugmentor> augmentorType = annotation.value();
                try {
                    augmentor = beanFactory.createBean(augmentorType);
                } catch (Exception e) {
                    LOG.error("Failed to create query augmentor from class name {}", augmentorType);
                    throw new IllegalStateException("Failed to create query augmentor", e);
                }
            }

            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(domainType);
            return new AugmentableQueryRepositoryImpl(entityInformation, em, repositoryInterface, augmentor);
        } else {
            return super.getTargetRepository(information);
        }
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        LOG.debug("AugmentableQueryRepositoryFactory.getRepositoryBaseClass");

        if (isAugmentableRepository(metadata.getRepositoryInterface())) {
            LOG.debug("Returning AugmentableQueryRepositoryImpl.class for " + metadata.getRepositoryInterface());
            return AugmentableQueryRepositoryImpl.class;
        } else {
            return super.getRepositoryBaseClass(metadata);
        }
    }


    private boolean isAugmentableRepository(Class<?> repositoryInterface) {
        return AnnotationUtils.findAnnotation(repositoryInterface, QueryAugmentorType.class) != null;
    }
}
