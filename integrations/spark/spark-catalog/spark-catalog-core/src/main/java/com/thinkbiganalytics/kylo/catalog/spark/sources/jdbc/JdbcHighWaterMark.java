package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;

import org.apache.spark.AccumulableParam;
import org.apache.spark.SparkContext;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;

/**
 * Records the maximum value seen for a specific high water mark.
 *
 * <p>The initial value (see {@link SparkContext#accumulable(Object, String, AccumulableParam) SparkContext}) should be constructed with a {@link KyloCatalogClient}. This ensures that the high water
 * mark value is updated globally when the Spark job finishes.</p>
 *
 * <p>The Spark executors will create instances with the default constructor to find the local maximum value. Then the Spark driver will accumulate these instances to find the global maximum value.
 * The global maximum value will be added to the instance constructed with the {@link KyloCatalogClient} (see above), and the high water mark value will be updated.</p>
 *
 * @see JdbcHighWaterMarkAccumulableParam
 * @see JdbcHighWaterMarkVisitor
 */
public class JdbcHighWaterMark implements Serializable {

    private static final long serialVersionUID = 5807794909985041453L;

    /**
     * Kylo Catalog client to be updated with new high water mark value
     */
    @Nullable
    private transient KyloCatalogClient<?> client;

    /**
     * Function to convert {@code value} to a high water mark value
     */
    @Nullable
    private transient Function1<Long, String> formatter;

    /**
     * Name of the high water mark to be updated in the Kylo Catalog client
     */
    @Nullable
    private String name;

    /**
     * Maximum value seen
     */
    @Nullable
    private Long value;

    /**
     * Constructs a {@code JdbcHighWaterMark} for use in Spark executors.
     */
    public JdbcHighWaterMark() {
        client = null;
        name = null;
    }

    /**
     * Constructs a {@code JdbcHighWaterMark} for the specified high water mark.
     *
     * <p>Use this instance as the initial value for {@link SparkContext#accumulable(Object, String, AccumulableParam) SparkContext}.</p>
     *
     * @param highWaterMarkName name of the high water mark
     * @param client            Kylo Catalog client
     */
    public JdbcHighWaterMark(@Nonnull final String highWaterMarkName, @Nonnull final KyloCatalogClient<?> client) {
        this.name = highWaterMarkName;
        this.client = client;
    }

    /**
     * Merges the specified value with this high water mark.
     */
    public void accumulate(@Nullable final Long value) {
        if (value != null && (this.value == null || this.value < value)) {
            if (client != null) {
                final String highWaterMarkValue = (formatter != null) ? formatter.apply(value) : value.toString();
                client.setHighWaterMarks(Collections.singletonMap(name, highWaterMarkValue));
            }
            this.value = value;
        }
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        return other instanceof JdbcHighWaterMark && Objects.equals(name, ((JdbcHighWaterMark) other).name) && Objects.equals(value, ((JdbcHighWaterMark) other).value);
    }

    /**
     * Gets the name of the high water mark.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Gets the current raw value.
     */
    @Nullable
    public Long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * Sets the high water mark value formatter.
     */
    public void setFormatter(@Nullable final Function1<Long, String> formatter) {
        this.formatter = formatter;
    }

    @Override
    public String toString() {
        return "JdbcHighWaterMark{name='" + name + "', value=" + value + '}';
    }
}
