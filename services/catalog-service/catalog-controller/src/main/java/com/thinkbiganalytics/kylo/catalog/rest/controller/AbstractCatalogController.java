package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
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

import com.thinkbiganalytics.kylo.catalog.CatalogException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * A basic REST controller for catalog APIs.
 */
public abstract class AbstractCatalogController {

    @Autowired
    @Qualifier("catalogMessages")
    MessageSource messages;

    @Inject
    HttpServletRequest request;

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    protected String getMessage(@Nonnull final String code) {
        return getMessage(code, (Object[]) null);
    }

    /**
     * Gets the specified message in the current locale with the specified arguments.
     */
    @Nonnull
    protected String getMessage(@Nonnull final String code, @Nullable final Object... args) {
        return messages.getMessage(code, args, RequestContextUtils.getLocale(request));
    }

    /**
     * Gets the localized message of the specified exception.
     */
    @Nonnull
    protected String getMessage(@Nonnull final CatalogException exception) {
        return exception.getLocalizedMessage(messages, RequestContextUtils.getLocale(request));
    }
}
