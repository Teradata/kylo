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

import com.google.common.base.Throwables;
import com.thinkbiganalytics.kylo.spark.job.tasks.ChainableSupplier;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public class DefaultSparkJobContextTest {

    /**
     * Spark job cache service
     */
    @Nonnull
    private final SparkJobCacheService cache = new SparkJobCacheService();

    /**
     * Executors for tasks and publisher
     */
    @Nonnull
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Before
    public void startUp() {
        cache.setResultTimeToLive(10000);
    }

    @After
    public void cleanUp() {
        cache.shutdown();
        executor.shutdownNow();
    }

    /**
     * Verify handling of <i>ERROR</i> responses.
     */
    @Test(expected = CompletionException.class)
    public void testError() {
        // Mock task
        final ChainableSupplier<SparkJobResponse> task = () -> {
            throw new UnsupportedOperationException();
        };

        // Test subscribing
        final DefaultSparkJobContext context = DefaultSparkJobContext.create(task, cache, executor);
        take(context);
    }

    /**
     * Verify handling of a <i>SUCCESS</i> response.
     */
    @Test
    public void testSuccess() {
        // Mock response
        final SparkJobResponse response = new SparkJobResponse();
        response.setId("mock-job");
        response.setResult(new SparkJobResult());
        response.setStatus(SparkJobResponse.Status.SUCCESS);

        // Test first subscriber
        final DefaultSparkJobContext context = DefaultSparkJobContext.create(() -> response, cache, executor);
        Assert.assertNotNull(take(context));

        // Test second subscriber
        Assert.assertEquals(response.getResult(), take(context).getResult());
    }

    /**
     * Retrieves the next item from the specified publisher.
     */
    @Nonnull
    private <T> T take(@Nonnull final Publisher<T> publisher) {
        // Lock until a result is received
        final Lock lock = new ReentrantLock();
        final Condition hasResult = lock.newCondition();

        // Subscribe and await the result
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<T> next = new AtomicReference<>();

        lock.lock();
        try {
            publisher.subscribe(new Subscriber<T>() {
                private Subscription subscription;

                @Override
                public void onSubscribe(final Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(final T t) {
                    lock.lock();
                    try {
                        next.set(t);
                        subscription.cancel();
                        hasResult.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public void onError(final Throwable t) {
                    lock.lock();
                    try {
                        error.set(t);
                        hasResult.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public void onComplete() {
                    lock.lock();
                    try {
                        hasResult.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            hasResult.await(10, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Assert.fail("Timeout waiting for response from publisher");
        } finally {
            lock.unlock();
        }

        // Return the result
        if (error.get() != null) {
            throw Throwables.propagate(error.get());
        } else if (next.get() != null) {
            return next.get();
        } else {
            throw new NoSuchElementException();
        }
    }
}
