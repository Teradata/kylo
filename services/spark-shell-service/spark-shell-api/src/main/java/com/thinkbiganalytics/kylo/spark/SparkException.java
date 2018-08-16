package com.thinkbiganalytics.kylo.spark;

/*-
 * #%L
 * Spark Shell Service API
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Throw to indicate an error related to Spark.
 */
public class SparkException extends RuntimeException {

    private static final long serialVersionUID = 9013433566074799789L;

    /**
     * Parameter arguments for the message
     */
    @Nullable
    private final Serializable[] args;

    /**
     * Message code
     */
    @Nullable
    private final String code;

    /**
     * Construct a {@code SparkException}.
     */
    public SparkException() {
        args = null;
        code = null;
    }

    /**
     * Construct a {@code SparkException} with the specified cause.
     */
    public SparkException(final Throwable cause) {
        super(cause);
        args = null;
        code = null;
    }

    /**
     * Construct a {@code SparkException} with the specified detail message or message code.
     */
    public SparkException(final String message) {
        // If message contains whitespace then it's the detail message, otherwise it's the message code
        super(StringUtils.containsWhitespace(message) ? message : null);
        args = null;
        code = (getMessage() == null) ? message : null;
    }

    /**
     * Construct a {@code SparkException} with the specified detail message or message code, and cause.
     */
    public SparkException(final String message, final Throwable cause) {
        // If message contains whitespace then it's the detail message, otherwise it's the message code
        super(StringUtils.containsWhitespace(message) ? message : null, cause);
        args = null;
        code = (getMessage() == null) ? message : null;
    }

    /**
     * Construct a {@code SparkException} with the specified message code and arguments.
     */
    public SparkException(@Nonnull final String code, final Object... args) {
        super();
        this.code = code;
        this.args = Arrays.stream(args).map(arg -> (arg instanceof Serializable) ? (Serializable) arg : arg.toString()).toArray(Serializable[]::new);

        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            initCause((Throwable) args[args.length - 1]);
        }
    }

    /**
     * Gets a localized version of the message from the specified message source.
     */
    @Nonnull
    public String getLocalizedMessage(@Nonnull final MessageSource messageSource, @Nonnull final Locale locale) {
        if (code != null) {
            try {
                return messageSource.getMessage(code, args, locale);
            } catch (final NoSuchMessageException e) {
                return code;
            }
        } else if (getMessage() != null) {
            return getMessage();
        } else {
            return getClass().getSimpleName();
        }
    }

    @Override
    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        } else if (code != null) {
            return code + (args != null ? " " + Arrays.toString(args) : "");
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final String className = getClass().getName();
        final String message = getLocalizedMessage();
        if (code != null && message != null) {
            return className + ": [" + code + "] " + message;
        } else if (code != null) {
            return className + ": " + code;
        } else if (message != null) {
            return className + ": " + message;
        } else {
            return className;
        }
    }
}
