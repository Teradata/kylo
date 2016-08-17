package com.thinkbiganalytics.jobrepo.config;


import java.util.function.Supplier;

/**
 * Created by sr186054 on 8/17/16.
 */
public interface OperationalMetadataAccess {

    /**
     *
     * @param function
     * @param <R>
     * @return
     */
    <R> R commit(Supplier<R> function);

    /**
     *
     * @param function
     * @param <R>
     * @return
     */
    <R> R read(Supplier<R> function);
}
