package com.thinkbiganalytics.kylo.catalog.datasource;

/*-
 * #%L
 * kylo-catalog-core
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resolves placeholders using a {@link DataSource}.
 */
public class DataSourcePlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

    @Nonnull
    private final DataSetTemplate template;

    public DataSourcePlaceholderResolver(@Nonnull final DataSource dataSource) {
        template = DataSourceUtil.mergeTemplates(dataSource);
    }

    @Nullable
    @Override
    public String resolvePlaceholder(@Nonnull final String s) {
        if ("files".equals(s)) {
            return (template.getFiles() != null) ? StringUtils.join(template.getFiles(), ",") : "";
        }
        if ("format".equals(s)) {
            return (template.getFormat() != null) ? template.getFormat() : "";
        }
        if ("jars".equals(s)) {
            return (template.getJars() != null) ? StringUtils.join(template.getJars(), ",") : "";
        }
        if ("paths".equals(s)) {
            return (template.getPaths() != null) ? StringUtils.join(template.getPaths(), ",") : "";
        }
        return (template.getOptions() != null) ? template.getOptions().getOrDefault(s, "") : "";
    }
}
