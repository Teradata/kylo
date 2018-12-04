package com.thinkbiganalytics.exceptions;

/*-
 * #%L
 * kylo-commons-util
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


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Hashing Utility
 */
public class ExceptionTransformerTest {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTransformerTest.class);

    @Test(expected = IllegalStateException.class)
    public void testBadRuntimeException() {
        ExceptionTransformer<BadRuntimeException> exceptionTransformer =
            new ExceptionTransformer<>(
                BadRuntimeException.class,
                SQLException.class);

        SQLException s = new SQLException("My custom SQL Exception");
        Throwable actual = exceptionTransformer.transformException(s);
        assertThat(actual).hasCause(new SQLException());
    }

    @Test
    public void testCatchSqlException() {
        ExceptionTransformer<CustomRuntimeException> exceptionTransformer =
            new ExceptionTransformer<>(
                CustomRuntimeException.class,
                SQLException.class);

        SQLException sqlException = new SQLException("My custom SQL Exception");
        Throwable actual = exceptionTransformer.transformException(sqlException);
        assertThat(actual).isInstanceOf(CustomRuntimeException.class);
        assertThat(actual).hasCause(sqlException);
    }

    @Test
    public void testCatchValidSqlExceptionWithCause() {
        ExceptionTransformer<CustomRuntimeException> exceptionTransformer =
            new ExceptionTransformer<>(
                CustomRuntimeException.class,
                SQLException.class,
                SocketException.class);

        SocketException socketException = new SocketException("My custom SocketException");
        SQLException sqlException = new SQLException("My custom SQL Exception", socketException);
        Throwable actual = exceptionTransformer.transformException(sqlException);
        assertThat(actual).isInstanceOf(CustomRuntimeException.class);
        assertThat(actual).hasCause(sqlException);
        assertThat(ExceptionUtils.indexOfThrowable(actual, socketException.getClass())).isNotNegative();
    }

    @Test
    public void testCatchInvalidSqlExceptionWithCause() {
        ExceptionTransformer<CustomRuntimeException> exceptionNotFoundTransformer =
            new ExceptionTransformer<>(
                CustomRuntimeException.class,
                BadRuntimeException.class,
                SocketException.class);

        SocketException socketException = new SocketException("My custom SocketException");
        SQLException sqlException = new SQLException("My custom SQL Exception", socketException);
        Throwable actual = exceptionNotFoundTransformer.transformException(sqlException);
        assertThat(actual).isNotInstanceOf(CustomRuntimeException.class);

        // check second cause not found either
        exceptionNotFoundTransformer =
            new ExceptionTransformer<>(
                CustomRuntimeException.class,
                SQLException.class,
                BadRuntimeException.class);

        actual = exceptionNotFoundTransformer.transformException(sqlException);
        assertThat(actual).isNotInstanceOf(CustomRuntimeException.class);
    }


    public static class BadRuntimeException extends RuntimeException {

    }

    public static class CustomRuntimeException extends RuntimeException {

        public CustomRuntimeException() {
            super();
        }

        public CustomRuntimeException(String s) {
            super(s);
        }

        public CustomRuntimeException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public CustomRuntimeException(Throwable throwable) {
            super(throwable);
        }
    }
}
