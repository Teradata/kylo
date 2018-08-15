package com.thinkbiganalytics.kylo.spark.job;

/*-
 * #%L
 * Spark Shell Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.spark.job.tasks.ChainableSupplier;
import com.thinkbiganalytics.kylo.spark.job.tasks.JobStatusFunction;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import reactor.core.processor.RingBufferProcessor;

/**
 * A Spark job context that uses a {@link Future} to retrieve the Spark job results.
 */
public class DefaultSparkJobContext implements SparkJobContext {

    /**
     * Task for retrieving the Spark job results
     */
    @Nullable
    private Future<SparkJobStatus> future;

    /**
     * Identifier for this context
     */
    @Nonnull
    private final String id;

    /**
     * Publishes the Spark job results to subscribers
     */
    @Nonnull
    private final Publisher<SparkJobStatus> publisher;

    /**
     * Spark's identifier for the job
     */
    @Nullable
    private String sparkJobId;

    /**
     * Creates a {@code DefaultSparkJobContext} using the specified task to execute the Spark job.
     */
    public static DefaultSparkJobContext create(@Nonnull final ChainableSupplier<SparkJobResponse> responseTask, @Nonnull final SparkJobCacheService cache, @Nonnull final ExecutorService executor) {
        // Create context
        final String id = UUID.randomUUID().toString();
        final Processor<SparkJobStatus, SparkJobStatus> processor = RingBufferProcessor.create(executor, false);
        final DefaultSparkJobContext context = new DefaultSparkJobContext(id, processor);

        // Start task
        final ChainableSupplier<SparkJobStatus> statusTask = responseTask
            .andThen(response -> {
                context.sparkJobId = response.getId();
                return response;
            })
            .andThen(new JobStatusFunction(cache));
        final CompletableFuture<SparkJobStatus> future = CompletableFuture.supplyAsync(statusTask, executor)
            .whenComplete((response, error) -> {
                if (response != null) {
                    processor.onNext(response);
                    processor.onComplete();
                } else if (error != null) {
                    processor.onError(error);
                } else {
                    processor.onError(new NoSuchElementException());
                }
            });
        context.setFuture(future);
        return context;
    }

    /**
     * Construct a {@code DefaultSparkJobContext}.
     */
    private DefaultSparkJobContext(@Nonnull final String id, @Nonnull final Publisher<SparkJobStatus> publisher) {
        this.id = id;
        this.publisher = publisher;
    }

    /**
     * Cancels the task for retrieving the Spark job response.
     */
    public void cancel() {
        if (future != null) {
            future.cancel(true);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns Spark's identifier for the job.
     */
    @Nullable
    public String getSparkJobId() {
        return sparkJobId;
    }

    /**
     * Sets the future for retrieving the Spark job status.
     */
    public void setFuture(@Nonnull final Future<SparkJobStatus> future) {
        this.future = future;
    }

    @Override
    public void subscribe(@Nonnull final Subscriber<? super SparkJobStatus> subscriber) {
        publisher.subscribe(subscriber);
    }
}
