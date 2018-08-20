package com.thinkbiganalytics.schema;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
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

import com.thinkbiganalytics.discovery.schema.JdbcSchemaParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Provider factory that returns instances of {@link JdbcSchemaParser}.
 */
@Component
public class JdbcSchemaParserProvider {

    /**
     * List of available JDBC schema parsers
     */
    @Nonnull
    private final List<JdbcSchemaParser> parsers;

    /**
     * Constructs a {@code JdbcSchemaParserProvider}.
     */
    @Autowired
    public JdbcSchemaParserProvider(@Nonnull final List<JdbcSchemaParser> parsers) {
        this.parsers = parsers;
    }

    /**
     * Gets the schema parser for the specified JDBC URL.
     *
     * @param url JDBC URL
     * @return the schema parser
     * @throws SQLException if a database access error occurs or no schema parsers accept the URL
     */
    @Nonnull
    public JdbcSchemaParser getSchemaParser(@Nonnull final String url) throws SQLException {
        for (final JdbcSchemaParser parser : parsers) {
            if (parser.acceptsURL(url)) {
                return parser;
            }
        }
        throw new SQLException("No schema parsers accept URL: " + url);
    }
}
