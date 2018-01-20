package com.thinkbiganalytics.spark.jdbc;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static utility methods for {@code Converter} instances.
 */
public class Converters {

    /**
     * Maximum string length to return for long column values.
     */
    private static final int MAX_LENGTH = 255;

    /**
     * Converts {@code BLOB} columns to {@code byte} arrays.
     */
    public static Converter blobType() {
        return StandardConverter.BLOB;
    }

    /**
     * Converts {@code CLOB} columns to strings.
     */
    @Nonnull
    public static Converter clobType() {
        return StandardConverter.CLOB;
    }

    /**
     * Gets the JDBC object from the result set.
     */
    @Nonnull
    public static Converter identity() {
        return StandardConverter.IDENTITY;
    }

    /**
     * Gets the column as an {@code Integer}.
     */
    @Nonnull
    public static Converter integerType() {
        return StandardConverter.INTEGER;
    }

    /**
     * Gets the column as a {@code Timestamp}.
     */
    @Nonnull
    public static Converter timestampType() {
        return StandardConverter.TIMESTAMP;
    }

    /**
     * Instances of {@code Converters} should not be constructed.
     */
    private Converters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts standard JDBC objects to Spark objects.
     */
    private enum StandardConverter implements Converter {

        /**
         * @see Converters#blobType()
         */
        BLOB {
            @Nullable
            @Override
            public Object convert(@Nonnull final ResultSet rs, final int i) throws SQLException {
                final Blob blob = rs.getBlob(i);
                if (rs.wasNull()) {
                    return null;
                } else {
                    final long length = blob.length();
                    return blob.getBytes(1, (length < MAX_LENGTH) ? (int) length : MAX_LENGTH);
                }
            }

            @Override
            public String toString() {
                return "Converters#blobType()";
            }
        },

        /**
         * @see Converters#clobType()
         */
        CLOB {
            @Nullable
            @Override
            public Object convert(@Nonnull final ResultSet rs, final int i) throws SQLException {
                final Clob clob = rs.getClob(i);
                if (rs.wasNull()) {
                    return null;
                } else {
                    final long length = clob.length();
                    return (length < MAX_LENGTH) ? clob.getSubString(1, (int) length) : clob.getSubString(1, MAX_LENGTH) + "…";
                }
            }

            @Override
            public String toString() {
                return "Converters#clobType()";
            }
        },

        /**
         * @see Converters#identity()
         */
        IDENTITY {
            @Nullable
            @Override
            public Object convert(@Nonnull final ResultSet rs, final int i) throws SQLException {
                final Object value = rs.getObject(i);
                return rs.wasNull() ? null : value;
            }

            @Override
            public String toString() {
                return "Converters#identity()";
            }
        },

        /**
         * @see Converters#integerType()
         */
        INTEGER {
            @Nullable
            @Override
            public Object convert(@Nonnull final ResultSet rs, final int i) throws SQLException {
                final int value = rs.getInt(i);
                return rs.wasNull() ? null : value;
            }

            @Override
            public String toString() {
                return "Converters#integerType()";
            }
        },

        /**
         * @see Converters#timestampType()
         */
        TIMESTAMP {
            @Nullable
            @Override
            public Object convert(@Nonnull final ResultSet rs, final int i) throws SQLException {
                final Timestamp value = rs.getTimestamp(i);
                return rs.wasNull() ? null : value;
            }

            @Override
            public String toString() {
                return "Converters#timestampType()";
            }
        }
    }
}
