package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.google.common.annotations.VisibleForTesting;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcHighWaterMark;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcHighWaterMarkAccumulableParam;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcHighWaterMarkVisitor;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcRelationProvider;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.Option;
import scala.runtime.AbstractFunction1;

/**
 * Base implementation of a data set provider that can read from and write to JDBC tables.
 *
 * @param <T> Spark {@code DataFrame} class
 */
public abstract class AbstractJdbcDataSetProvider<T> implements DataSetProvider<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcDataSetProvider.class);

    /**
     * Option key to specify the date field for incremental loading
     */
    private static final String DATE_FIELD_OPTION = "datefield";

    /**
     * Option key to specify the high water mark name for reading files
     */
    private static final String HIGH_WATER_MARK_OPTION = "highwatermark";

    /**
     * Option key to delay loading of rows from the previous specified seconds
     */
    private static final String OVERLAP_OPTION = "overlap";

    @Override
    public boolean supportsFormat(@Nonnull final String source) {
        return "jdbc".equalsIgnoreCase(source)
               || "org.apache.spark.sql.jdbc".equals(source)
               || "org.apache.spark.sql.jdbc.DefaultSource".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc.DefaultSource".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider".equals(source);
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        // Set url for PostgreSQL databases
        final Option<String> catalog = options.getOption("PGDBNAME");
        final Option<String> url = options.getOption("url");
        if (catalog.isDefined() && url.isDefined() && url.get().startsWith("jdbc:postgres://")) {
            final String[] urlSplit = url.get().split("\\?", 2);
            final String[] pathSplit = urlSplit[0].substring(16).split("/", 2);

            if (pathSplit.length == 1 || StringUtils.equalsAny(pathSplit[1], "", "/")) {
                String catalogUrl = "jdbc:postgres://" + pathSplit[0] + "/" + urlEncode(catalog.get());
                if (urlSplit.length == 2) {
                    catalogUrl += "?" + urlSplit[1];
                }
                options.setOption("url", catalogUrl);
            }
        }

        // Load data set
        final DataFrameReader reader = SparkUtil.prepareDataFrameReader(getDataFrameReader(client, options), options, null);
        reader.format(JdbcRelationProvider.class.getName());
        T dataSet = load(reader);

        // Handle high water mark
        final String dateField = SparkUtil.getOrElse(options.getOption(DATE_FIELD_OPTION), null);
        final String highWaterMarkKey = SparkUtil.getOrElse(options.getOption(HIGH_WATER_MARK_OPTION), null);
        final Long overlap = getOverlap(options);

        if (dateField != null && highWaterMarkKey != null) {
            final JdbcHighWaterMark initialValue = createHighWaterMark(highWaterMarkKey, client);
            dataSet = filterByDateTime(dataSet, dateField, initialValue.getValue(), overlap);
            dataSet = updateHighWaterMark(dataSet, dateField, initialValue, client);
        } else if (highWaterMarkKey != null) {
            log.warn("Ignoring '{}' option because '{}' option was not specified", HIGH_WATER_MARK_OPTION, DATE_FIELD_OPTION);
        } else if (overlap != null) {
            log.warn("Ignoring '{}' option because '{}' and '{}' options were not specified", OVERLAP_OPTION, DATE_FIELD_OPTION, HIGH_WATER_MARK_OPTION);
        }

        return dataSet;
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        // Extract JDBC options
        final String url = DataSetUtil.getOptionOrThrow(options, "url", "Option 'url' is required");
        final String table = DataSetUtil.getOptionOrThrow(options, "dbtable", "Option 'dbtable' is required");

        final Properties properties = new Properties();
        properties.putAll(options.getOptions());

        // Write to JDBC table
        final DataFrameWriter writer = SparkUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options, null);
        writer.jdbc(url, table, properties);
    }

    /**
     * Creates an {@link Accumulable} shared variable with a name for display in the Spark UI.
     */
    @Nonnull
    protected abstract <R, P1> Accumulable<R, P1> accumulable(@Nonnull R initialValue, @Nonnull String name, @Nonnull AccumulableParam<R, P1> param, @Nonnull KyloCatalogClient<T> client);

    /**
     * Filters rows using the specified condition.
     */
    @Nonnull
    protected abstract T filter(@Nonnull T dataSet, @Nonnull Column condition);

    /**
     * Gets a reader from the specified client.
     *
     * <p>The options, format, and scheme will be applied to the reader before loading.</p>
     */
    @Nonnull
    protected abstract DataFrameReader getDataFrameReader(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options);

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Loads a data set using the specified reader and filter.
     */
    @Nonnull
    protected abstract T load(@Nonnull DataFrameReader reader);

    /**
     * Applies the specified function to the specified field of the data set.
     */
    @Nonnull
    protected abstract T map(@Nonnull T dataSet, @Nonnull String fieldName, @Nonnull Function1 function, @Nonnull DataType returnType);

    /**
     * Returns the schema of the specified data set.
     */
    @Nonnull
    protected abstract StructType schema(@Nonnull T dataSet);

    /**
     * Creates a {@link JdbcHighWaterMark} using the specified high water mark.
     *
     * <p>The value is initialized using the {@link KyloCatalogClient}.</p>
     */
    @Nonnull
    @VisibleForTesting
    JdbcHighWaterMark createHighWaterMark(@Nonnull final String highWaterMarkKey, @Nonnull final KyloCatalogClient<T> client) {
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark(highWaterMarkKey, client);
        highWaterMark.setFormatter(new LongToDateTime());

        // Set value
        final String value = client.getHighWaterMarks().get(highWaterMarkKey);
        if (value != null) {
            try {
                highWaterMark.accumulate(ISODateTimeFormat.dateTimeParser().withZoneUTC().parseMillis(value));
            } catch (final IllegalArgumentException e) {
                throw new KyloCatalogException("Invalid value for high water mark " + highWaterMarkKey + ": " + value, e);
            }
        }

        return highWaterMark;
    }

    /**
     * Filters the specified data set using the specified date field.
     */
    @Nonnull
    @VisibleForTesting
    T filterByDateTime(@Nonnull final T dataSet, @Nonnull final String fieldName, @Nullable final Long value, @Nullable final Long overlap) {
        long startTime = 0;
        long endTime = DateTimeUtils.currentTimeMillis();

        // Parse high water mark
        if (value != null) {
            if (value < endTime) {
                startTime = value;
            } else {
                log.warn("Value for high water mark is the future: {}", value);
                startTime = endTime;
            }
        }

        // Parse overlap
        if (overlap != null) {
            startTime = Math.max(startTime - overlap, 0);
            endTime -= overlap;
        }

        // Return filter
        final Column dateColumn = new Column(fieldName);
        final Column startFilter = (startTime > 0) ? dateColumn.gt(functions.lit(new Timestamp(startTime))) : null;
        final Column endFilter = dateColumn.lt(functions.lit(new Timestamp(endTime)));
        return filter(dataSet, (startFilter != null) ? startFilter.and(endFilter) : endFilter);
    }

    /**
     * Gets the overlap from the specified options.
     */
    @Nullable
    @VisibleForTesting
    Long getOverlap(@Nonnull final DataSetOptions options) {
        final String overlap = SparkUtil.getOrElse(options.getOption(OVERLAP_OPTION), null);

        if (overlap != null) {
            try {
                return Math.abs(Long.parseLong(overlap) * 1000);
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid value for 'overlap' option: " + overlap);
            }
        } else {
            return null;
        }
    }

    /**
     * Scans the specified field and updates the specified high water mark.
     */
    @Nonnull
    @VisibleForTesting
    T updateHighWaterMark(@Nonnull final T dataSet, @Nonnull final String fieldName, @Nonnull final JdbcHighWaterMark highWaterMark, @Nonnull final KyloCatalogClient<T> client) {
        // Determine function to convert column to Long
        final DataType fieldType = schema(dataSet).apply(fieldName).dataType();
        final Function1<?, Long> toLong;

        if (fieldType == DataTypes.DateType) {
            toLong = new DateToLong();
        } else if (fieldType == DataTypes.TimestampType) {
            toLong = new TimestampToLong();
        } else {
            throw new KyloCatalogException("Unsupported column type for high water mark: " + fieldType);
        }

        // Create UDF and apply to field
        final String accumulableId = (highWaterMark.getName() != null) ? highWaterMark.getName() : UUID.randomUUID().toString();
        final Accumulable<JdbcHighWaterMark, Long> accumulable = accumulable(highWaterMark, accumulableId, new JdbcHighWaterMarkAccumulableParam(), client);

        final JdbcHighWaterMarkVisitor<?> visitor = new JdbcHighWaterMarkVisitor<>(accumulable, toLong);
        return map(dataSet, fieldName, visitor, fieldType);
    }

    @Nonnull
    private String urlEncode(@Nonnull final String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new KyloCatalogException(e.toString(), e);
        }
    }

    /**
     * Converts a {@link Date} value to a {@link Long} value.
     */
    private static class DateToLong extends AbstractFunction1<Date, Long> implements Serializable {

        private static final long serialVersionUID = -8050299509057508031L;

        @Nullable
        @Override
        public Long apply(@Nullable final Date value) {
            return (value != null) ? value.getTime() : null;
        }
    }

    /**
     * Converts a {@link Long} value to a date and time string.
     */
    private static class LongToDateTime extends AbstractFunction1<Long, String> implements Serializable {

        private static final long serialVersionUID = 3461139184234190000L;

        /**
         * Date and time format for high water mark values
         */
        private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").withZoneUTC();

        @Nullable
        @Override
        public String apply(@Nullable final Long value) {
            return (value != null) ? FORMATTER.print(value) : null;
        }
    }

    /**
     * Converts a {@link Timestamp} value to a {@link Long} value.
     */
    private static class TimestampToLong extends AbstractFunction1<Timestamp, Long> implements Serializable {

        private static final long serialVersionUID = -2438416767972800004L;

        @Nullable
        @Override
        public Long apply(@Nullable final Timestamp value) {
            return (value != null) ? value.getTime() : null;
        }
    }
}
