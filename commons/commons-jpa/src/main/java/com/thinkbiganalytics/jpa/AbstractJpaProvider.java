package com.thinkbiganalytics.jpa;


import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractJpaProvider<T, PK extends Serializable> implements BaseJpaProvider<T, PK> {

    protected Class<T> entityClass;

    public abstract Class<? extends T> getEntityClass();

    //@PersistenceContext
    //@Inject
    //@Qualifier("metadataEntityManager")
    //protected EntityManager entityManager;

    public abstract EntityManager getEntityManager();

    public AbstractJpaProvider() {
        this.entityClass = (Class<T>) getEntityClass();
    }

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