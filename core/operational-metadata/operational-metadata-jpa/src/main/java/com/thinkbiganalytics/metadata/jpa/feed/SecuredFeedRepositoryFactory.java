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
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * Created by ru186002 on 05/04/2017.
 */
class SecuredFeedRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SecuredFeedRepositoryFactory.class);

    private final EntityManager em;

    SecuredFeedRepositoryFactory(EntityManager em) {
        super(em);
        this.em = em;
        LOG.debug("SecuredFeedRepositoryFactory.SecuredFeedRepositoryFactory");
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        LOG.debug("SecuredFeedRepositoryFactory.getTargetRepository");

        Class<?> repositoryInterface = information.getRepositoryInterface();

        Class<?> domainType = information.getDomainType();
        JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(domainType);

        if (isSecuredRepository(repositoryInterface)) {
            LOG.debug("Creating SecuredFeedRepositoryImpl for {}", domainType);
            return new SecuredFeedRepositoryImpl(entityInformation, em, repositoryInterface);
        } else {
            return super.getTargetRepository(information);
        }
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        LOG.debug("SecuredFeedRepositoryFactory.getRepositoryBaseClass");

        if (isSecuredRepository(metadata.getRepositoryInterface())) {
            LOG.debug("Returning SecuredFeedRepositoryImpl.class for " + metadata.getRepositoryInterface());
            return SecuredFeedRepositoryImpl.class;
        } else {
            return super.getRepositoryBaseClass(metadata);
        }
    }


    private boolean isSecuredRepository(Class<?> repositoryInterface) {
        return SecuredFeedRepository.class.isAssignableFrom(repositoryInterface);
    }
}
