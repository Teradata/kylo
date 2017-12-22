package com.thinkbiganalytics.spark.util;

/*-
 * #%L
 * kylo-commons-scala-api
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

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import javax.annotation.Nonnull;

import scala.Function0;
import scala.Function1;
import scala.Serializable;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractFunction1;

/**
 * Static utility methods for interacting with Scala from Java
 */
public class ScalaUtil {

    /**
     * A serializable Scala function with no arguments.
     */
    private abstract static class GuavaFunction0<R> extends AbstractFunction0<R> implements Serializable {

        private static final long serialVersionUID = 258894881300511645L;
    }

    /**
     * Wraps the specified Guava {@link Supplier} as a Scala {@link Function0}.
     */
    public static <R> Function0<R> wrap(@Nonnull final Supplier<R> supplier) {
        return new GuavaFunction0<R>() {

            private static final long serialVersionUID = -6223479607518416561L;

            @Override
            public R apply() {
                return supplier.get();
            }

            @Override
            public String toString() {
                return "ScalaUtil#wrap()";
            }
        };
    }

    /**
     * A serializable Scala function with one argument.
     */
    private abstract static class GuavaFunction1<T1, R> extends AbstractFunction1<T1, R> implements Serializable {

        private static final long serialVersionUID = 605061399766933277L;
    }

    /**
     * Wraps the specified Guava {@link Function} as a Scala {@link Function1}.
     */
    public static <T1, R> Function1<T1, R> wrap(@Nonnull final Function<T1, R> function) {
        return new GuavaFunction1<T1, R>() {

            private static final long serialVersionUID = -6064687026877787966L;

            @Override
            public R apply(T1 v1) {
                return function.apply(v1);
            }

            @Override
            public String toString() {
                return "ScalaUtil#wrap()";
            }
        };
    }

    /**
     * Instances of {@code ScalaUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private ScalaUtil() {
        throw new UnsupportedOperationException();
    }
}
