package com.thinkbiganalytics.jdbc.util;

/*-
 * #%L
 * kylo-commons-jdbc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database utilities
 */
public class DatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    private static final String EMPTY_STRING = "";

    /**
     * <p> Get the database identifier quote string. This can be used around the field names when querying a table in the database. Useful for case sensitive column names. If connection is provided as
     * null, or database returns a null identifier, an empty string is returned. </p>
     *
     * @param conn Connection to database
     * @return quote string
     */
    public static String getDatabaseIdentifierQuoteString(Connection conn) {
        String dbIdentifierQuoteString = EMPTY_STRING;

        if (conn == null) {
            logger.warn("Identifier quote string from database requested, but connection provided is null.");
            return dbIdentifierQuoteString;
        }

        try {
            dbIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
        } catch (SQLException e) {
            logger.warn("Unable to get identifier quote string from database with connection {} [{}]", conn.toString(), e.getMessage());
        }

        if (dbIdentifierQuoteString == null) {
            dbIdentifierQuoteString = EMPTY_STRING;
        }

        logger.debug("Identifier quote string: {}", dbIdentifierQuoteString);
        return dbIdentifierQuoteString;
    }
}
