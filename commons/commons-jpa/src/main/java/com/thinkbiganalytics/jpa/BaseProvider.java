package com.thinkbiganalytics.jpa;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface BaseProvider<T, PK extends Serializable> {

    T create(T t);

    T findById(PK id);

    List<T> findAll();

    T update(T t);

    void delete(T t);

    void deleteById(PK id);

    PK resolveId(Serializable fid);

}