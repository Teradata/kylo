package com.thinkbiganalytics.kylo.catalog;

/*-
 * #%L
 * kylo-catalog-api
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

import org.springframework.context.MessageSource;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Throw to indicate an error related to the Kylo catalog.
 */
public class CatalogException extends RuntimeException {

    private static final long serialVersionUID = 5052620770812347955L;

    /**
     * Parameter arguments for the message
     */
    private final transient Object[] args;

    /**
     * Construct a {@code CatalogException} with the specified message code and arguments.
     */
    public CatalogException(final String code, final Object... args) {
        super(code);
        this.args = args;
    }

    /**
     * Construct a {@code CatalogException} with the specified message code and arguments, and cause.
     */
    public CatalogException(final Exception cause, final String code, final Object... args) {
        super(code, cause);
        this.args = args;
    }

    /**
     * Gets a localized version of the message from the specified message source.
     */
    public String getLocalizedMessage(@Nonnull final MessageSource messageSource, @Nullable final Locale locale) {
        return messageSource.getMessage(getMessage(), args, locale);
    }
}
