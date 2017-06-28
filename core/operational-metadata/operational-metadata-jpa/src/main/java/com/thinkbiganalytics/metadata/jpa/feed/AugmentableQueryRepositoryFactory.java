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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

/**
 * Factory which knows how to create AugmentableRepositories with QueryAugmentors
 */
class AugmentableQueryRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableQueryRepositoryFactory.class);

    private final EntityManager em;
    private AccessController accessController;

    AugmentableQueryRepositoryFactory(EntityManager em, AccessController accessController) {
        super(em);
        this.em = em;
        this.accessController = accessController;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        LOG.debug("AugmentableQueryRepositoryFactory.getTargetRepository");

        Class<?> repositoryInterface = information.getRepositoryInterface();

        if (isAugmentableRepository(repositoryInterface) && accessController.isEntityAccessControlled()) {
            Class<?> domainType = information.getDomainType();
            LOG.debug("Creating AugmentableQueryRepositoryImpl for repo interface {} and domain class {}", repositoryInterface, domainType);

            RepositoryType annotation = AnnotationUtils.findAnnotation(repositoryInterface, RepositoryType.class);
            Class<? extends AugmentableQueryRepositoryImpl> repoType = annotation.value();
            try {
                assertAugmentableRepositoryImplementsDeclaredMethods(repositoryInterface, repoType);
                Constructor<? extends AugmentableQueryRepositoryImpl> constructor = repoType.getConstructor(JpaEntityInformation.class, EntityManager.class);
                return constructor.newInstance(getEntityInformation(domainType), em);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Failed to create Augmentable Repository %s", repoType), e);
            }
        } else {
            return super.getTargetRepository(information);
        }
    }

    /**
     * Augmentable Repository must implement methods declared by repository interface, otherwise non-implemented methods will be called on default
     * repository implementation, such as SimpleJpaRepository, which are guaranteed not to be augmented e.g. not augmented for entity level access control.
     * Here we cannot guarantee correct implementation, but implementation is the minimum contract.
     */
    private void assertAugmentableRepositoryImplementsDeclaredMethods(Class<?> repositoryInterface, Class<? extends AugmentableQueryRepositoryImpl> repoType) {
        Method[] methods = repositoryInterface.getDeclaredMethods();
        for (Method method : methods) {
            if (AnnotationUtils.findAnnotation(method, Query.class) != null) {
                continue;
            }
            if (AnnotationUtils.findAnnotation(method, Procedure.class) != null) {
                continue;
            }
            try {
                repoType.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(String.format("Augmentable Repository '%s' must implement method '%s' declared by repository interface '%s'", repoType, method.getName(), repositoryInterface));
            }
        }
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        LOG.debug("AugmentableQueryRepositoryFactory.getRepositoryBaseClass");

        if (isAugmentableRepository(metadata.getRepositoryInterface()) && accessController.isEntityAccessControlled()) {
            RepositoryType annotation = AnnotationUtils.findAnnotation(metadata.getRepositoryInterface(), RepositoryType.class);
            return annotation.value();
        } else {
            return super.getRepositoryBaseClass(metadata);
        }
    }


    private boolean isAugmentableRepository(Class<?> repositoryInterface) {
        return AnnotationUtils.findAnnotation(repositoryInterface, RepositoryType.class) != null;
    }
}
