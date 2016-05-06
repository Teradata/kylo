package com.thinkbiganalytics.metadata.jpa;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class BaseJpaProvider<T, PK extends Serializable> implements BaseProvider<T, PK> {

    protected Class<T> entityClass;

    public abstract Class<? extends T> getEntityClass();

    @PersistenceContext
    @Inject
    @Qualifier("metadataEntityManager")
    protected EntityManager entityManager;

    public BaseJpaProvider() {
        this.entityClass = (Class<T>)getEntityClass();
    }



    @Override
    public T create(T t) {
        this.entityManager.persist(t);
        return t;
    }

    @Override
    public T findById(PK id) {
        return this.entityManager.find(entityClass, id);
    }

    @Override
    public List<T> findAll(){
        return this.entityManager.createQuery("from "+entityClass.getName()).getResultList();
    }

    @Override
    public T update(T t) {
        return this.entityManager.merge(t);
    }

    @Override
    public void delete(T t) {
        t = this.entityManager.merge(t);
        this.entityManager.remove(t);
    }

    @Override
    public void deleteById(PK id) {
        T t = this.findById(id);
        if(id != null){
            delete(t);
        }
    }
}