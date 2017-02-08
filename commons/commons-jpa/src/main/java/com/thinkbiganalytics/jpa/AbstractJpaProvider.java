package com.thinkbiganalytics.jpa;

/*-
 * #%L
 * thinkbig-commons-jpa
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


import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

/**
 */
public abstract class AbstractJpaProvider<T, PK extends Serializable> implements BaseJpaProvider<T, PK> {

    protected Class<T> entityClass;

    public AbstractJpaProvider() {
        this.entityClass = (Class<T>) getEntityClass();
    }

    //@PersistenceContext
    //@Inject
    //@Qualifier("metadataEntityManager")
    //protected EntityManager entityManager;

    public abstract Class<? extends T> getEntityClass();

    public abstract EntityManager getEntityManager();

    @Override
    public T create(T t) {
        this.getEntityManager().persist(t);
        return t;
    }

    @Override
    public T findById(PK id) {
        return this.getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> findAll() {
        return this.getEntityManager().createQuery("from " + entityClass.getName()).getResultList();
    }

    @Override
    public T update(T t) {
        return this.getEntityManager().merge(t);
    }

    @Override
    public void delete(T t) {
        t = this.getEntityManager().merge(t);
        this.getEntityManager().remove(t);
    }

    @Override
    public void deleteById(PK id) {
        T t = this.findById(id);
        if (id != null) {
            delete(t);
        }
    }
}
