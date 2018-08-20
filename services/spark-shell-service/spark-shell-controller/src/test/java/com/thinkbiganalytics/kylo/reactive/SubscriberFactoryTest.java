package com.thinkbiganalytics.kylo.reactive;

/*-
 * #%L
 * Spark Shell Service Controllers
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

public class SubscriberFactoryTest {

    /**
     * Verify a {@link SingleSubscriber} that is cancelled.
     */
    @Test
    public void singleWithCancel() {
        // Create subscriber
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<Object> next = new AtomicReference<>();
        final CancellableSubscriber<Object> subscriber = SubscriberFactory.single(new SingleSubscriber<Object>() {
            @Override
            public void onError(@Nonnull final Throwable t) {
                error.set(t);
            }

            @Override
            public void onSuccess(@Nonnull final Object o) {
                next.set(o);
            }
        });

        // Test onSubscribe
        final Subscription subscription = Mockito.mock(Subscription.class);
        subscriber.onSubscribe(subscription);
        Mockito.verify(subscription, Mockito.times(1)).request(1);

        // Test cancel
        subscriber.cancel();
        Mockito.verify(subscription, Mockito.times(1)).cancel();

        // Test onNext
        subscriber.onNext(new Object());
        Assert.assertNull(error.get());
        Assert.assertNull(next.get());

        // Test onComplete
        subscriber.onComplete();
        Assert.assertNull(error.get());
        Assert.assertNull(next.get());
    }

    /**
     * Verify a {@link SingleSubscriber} with an {@code onComplete} call.
     */
    @Test
    public void singleWithComplete() {
        // Create subscriber
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<Object> next = new AtomicReference<>();
        final Subscriber<Object> subscriber = SubscriberFactory.single(new SingleSubscriber<Object>() {
            @Override
            public void onError(@Nonnull final Throwable t) {
                error.set(t);
            }

            @Override
            public void onSuccess(@Nonnull final Object o) {
                next.set(o);
            }
        });

        // Test onSubscribe
        final Subscription subscription = Mockito.mock(Subscription.class);
        subscriber.onSubscribe(subscription);
        Mockito.verify(subscription, Mockito.times(1)).request(1);

        // Test onComplete
        subscriber.onComplete();
        Assert.assertThat(error.get(), CoreMatchers.instanceOf(NoSuchElementException.class));
        Assert.assertNull(next.get());
    }

    /**
     * Verify a {@link SingleSubscriber} with an {@code onError} call.
     */
    @Test
    public void singleWithError() {
        // Create subscriber
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<Object> next = new AtomicReference<>();
        final Subscriber<Object> subscriber = SubscriberFactory.single(new SingleSubscriber<Object>() {
            @Override
            public void onError(@Nonnull final Throwable t) {
                error.set(t);
            }

            @Override
            public void onSuccess(@Nonnull final Object o) {
                next.set(o);
            }
        });

        // Test onSubscribe
        final Subscription subscription = Mockito.mock(Subscription.class);
        subscriber.onSubscribe(subscription);
        Mockito.verify(subscription, Mockito.times(1)).request(1);

        // Test onError
        final RuntimeException exception = new RuntimeException();
        subscriber.onError(exception);
        Assert.assertEquals(exception, error.get());
        Assert.assertNull(next.get());
    }

    /**
     * Verify a {@link SingleSubscriber} with an {@code onNext} call.
     */
    @Test
    public void singleWithNext() {
        // Create subscriber
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicReference<Object> next = new AtomicReference<>();
        final Subscriber<Object> subscriber = SubscriberFactory.single(new SingleSubscriber<Object>() {
            @Override
            public void onError(@Nonnull final Throwable t) {
                error.set(t);
            }

            @Override
            public void onSuccess(@Nonnull final Object o) {
                next.set(o);
            }
        });

        // Test onSubscribe
        final Subscription subscription = Mockito.mock(Subscription.class);
        subscriber.onSubscribe(subscription);
        Mockito.verify(subscription, Mockito.times(1)).request(1);

        // Test onNext
        final Object value = new Object();
        subscriber.onNext(value);
        Mockito.verify(subscription, Mockito.times(1)).cancel();
        Assert.assertNull(error.get());
        Assert.assertEquals(value, next.get());

        // Test onComplete
        subscriber.onComplete();
        Assert.assertNull(error.get());
        Assert.assertEquals(value, next.get());
    }
}
