package com.thinkbiganalytics.hive.util;

/*-
 * #%L
 * thinkbig-commons-hive
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

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility methods for interacting with Hive.
 */
public class HiveUtils {

    /**
     * Instances of {@code HiveUtils} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private HiveUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Quotes the specified Hive identifier.
     *
     * @param identifier the Hive identifier to be quoted
     * @return the quoted Hive identifier
     */
    @Nonnull
    public static String quoteIdentifier(@Nonnull final String identifier) {
        return "`" + identifier.replaceAll("`", "``") + "`";
    }

    /**
     * Quotes the specified Hive identifiers. This method is commonly used to reference a specific table in a specific database.
     *
     * <p>If the first argument is not {@code null}, then the result will be in the format: `first`.`second`. Otherwise, the
     * format is the same as {@link #quoteIdentifier(String) quoteIdentifier(second)}</p>
     *
     * @param first  the first identifier, name of the database, or {@code null}
     * @param second the second identifier or name of the table
     * @return the string containing the quoted Hive identifiers
     */
    @Nonnull
    public static String quoteIdentifier(@Nullable final String first, @Nonnull final String second) {
        if (first != null) {
            return quoteIdentifier(first) + "." + quoteIdentifier(second);
        } else {
            return quoteIdentifier(second);
        }
    }

    /**
     * Quotes the specified string for use in a Hive query.
     *
     * @param string the string to be quoted
     * @return the quoted string
     */
    @Nonnull
    public static String quoteString(@Nonnull final String string) {
        try {
            final StringWriter writer = new StringWriter(string.length() + 2);
            writer.append('"');
            StringEscapeUtils.ESCAPE_JAVA.translate(string, writer);
            writer.append('"');
            return writer.toString();
        } catch (final IOException e) {
            throw new IllegalArgumentException("String contains invalid characters: " + string, e);
        }
    }
}
