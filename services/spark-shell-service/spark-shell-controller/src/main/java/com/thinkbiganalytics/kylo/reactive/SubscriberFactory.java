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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.container.AsyncResponse;

/**
 * Static utility methods for {@link Subscriber}.
 */
public class SubscriberFactory {

    /**
     * Creates a {@link Subscriber} that provides an asynchronous HTTP response.
     */
    @Nonnull
    public static <T> CancellableSubscriber<T> asyncResponse(@Nonnull final AsyncResponse asyncResponse, @Nonnull final AsyncResponseSubscriber<T> asyncSubscriber) {
        final AtomicBoolean lock = new AtomicBoolean();
        return single(new SingleSubscriber<T>() {
            @Override
            public void onError(@Nonnull final Throwable t) {
                if (lock.compareAndSet(false, true)) {
                    asyncSubscriber.onError(t, asyncResponse);
                }
            }

            @Override
            public void onSuccess(@Nonnull final T t) {
                if (lock.compareAndSet(false, true)) {
                    asyncSubscriber.onSuccess(t, asyncResponse);
                }
            }
        });
    }

    /**
     * Creates a {@link Subscriber} that provides an asynchronous HTTP response with the specified timeout.
     */
    @Nonnull
    public static <T> CancellableSubscriber<T> asyncResponseWithTimeout(@Nonnull final AsyncResponse asyncResponse, final long time, @Nonnull final TimeUnit unit,
                                                                        @Nonnull final AsyncResponseSubscriber<T> asyncSubscriber) {
        final CancellableSubscriber<T> reactiveSubscriber = asyncResponse(asyncResponse, asyncSubscriber);
        asyncResponse.setTimeout(time, unit);
        asyncResponse.setTimeoutHandler(timeoutResponse -> {
            reactiveSubscriber.cancel();
            reactiveSubscriber.onError(new TimeoutException());
        });
        return reactiveSubscriber;
    }

    /**
     * Creates a {@link Subscriber} that expects a single value response.
     *
     * @param subscriber callback for the response
     * @param <T>        type of element signaled
     * @return the subscriber
     */
    @Nonnull
    public static <T> CancellableSubscriber<T> single(@Nonnull final SingleSubscriber<T> subscriber) {
        final AtomicBoolean hasResponse = new AtomicBoolean();
        return new CancellableSubscriber<T>() {
            @Nullable
            private Subscription subscription;

            @Override
            public void cancel() {
                if (subscription != null) {
                    hasResponse.set(true);
                    subscription.cancel();
                    subscription = null;
                }
            }

            @Override
            public final void onComplete() {
                onError(new NoSuchElementException());
            }

            @Override
            public void onError(@Nonnull final Throwable t) {
                if (hasResponse.compareAndSet(false, true)) {
                    subscriber.onError(t);
                }
            }

            @Override
            public final void onNext(final T t) {
                if (hasResponse.compareAndSet(false, true)) {
                    cancel();
                    subscriber.onSuccess(t);
                }
            }

            @Override
            public final void onSubscribe(@Nonnull final Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }
        };
    }

    /**
     * Instances of {@code SubscriberFactory} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private SubscriberFactory() {
        throw new UnsupportedOperationException();
    }
}
