package com.thinkbiganalytics.spark.metadata;

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

import com.google.common.base.Supplier;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;

import org.apache.hadoop.hive.ql.session.SessionState;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.tools.nsc.interpreter.NamedParam;

/**
 * Executes a Spark job from a job request.
 */
public class SparkJobResultSupplier implements Supplier<SparkJobResult> {

    private static final XLogger log = XLoggerFactory.getXLogger(SparkJobResultSupplier.class);

    /**
     * Parameter to bind before executing script
     */
    @Nonnull
    private final List<NamedParam> bindings;

    /**
     * Spark engine for executing script
     */
    @Nonnull
    private final SparkScriptEngine engine;

    /**
     * Script to be executed
     */
    @Nonnull
    private final String script;

    /**
     * Hive session state to use during execution
     */
    @Nullable
    private SessionState sessionState;

    /**
     * Constructs a {@code SparkJobResultSupplier}.
     */
    public SparkJobResultSupplier(@Nonnull final SparkScriptEngine engine, @Nonnull final String script, @Nonnull final List<NamedParam> bindings) {
        this.engine = engine;
        this.script = script;
        this.bindings = bindings;
    }

    @Override
    public SparkJobResult get() {
        // SPARK-13180 Ensure SessionState is valid
        if (SessionState.get() == null && sessionState != null) {
            SessionState.setCurrentSessionState(sessionState);
        }

        // Execute script
        final Object result;
        try {
            result = this.engine.eval(script, bindings);
        } catch (final Exception cause) {
            throw log.throwing(new RuntimeException(cause));
        }

        if (result instanceof SparkJobResult) {
            return (SparkJobResult) result;
        } else {
            throw log.throwing(new IllegalStateException("Unexpected script result type: " + (result != null ? result.getClass() : null)));
        }
    }

    /**
     * Sets the Hive session state to use when executing the script.
     */
    public void setSessionState(@Nullable final SessionState sessionState) {
        this.sessionState = sessionState;
    }
}
