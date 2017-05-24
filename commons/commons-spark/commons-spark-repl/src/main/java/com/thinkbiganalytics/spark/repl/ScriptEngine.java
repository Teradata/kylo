package com.thinkbiganalytics.spark.repl;

/*-
 * #%L
 * thinkbig-commons-spark-repl
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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.spark.util.ArrayUtils;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import scala.tools.nsc.interpreter.NamedParam;

/**
 * Interface for an interpreter that compiles and evaluates Scala code containing a Spark job.
 *
 * <p>Scripts may access a {@link SparkContext} through the {@code sc} variable and a {@link SQLContext} through the
 * {@code sqlContext} variable.</p>
 *
 * <p>This class is <i>thread-safe</i> and ensures that only one script </p>
 */
public abstract class ScriptEngine {

    /**
     * End of line character
     */
    private static final byte[] END_LINE = new byte[]{'\n'};

    /**
     * Label used by the compiler to indicate a compile error
     */
    private static final byte[] LABEL = "<console>".getBytes(Charsets.UTF_8);

    /**
     * Separator between label, line number, and error message
     */
    private static final byte[] SEPARATOR = new byte[]{':'};

    /**
     * Exception thrown by the last script
     */
    @Nonnull
    private final AtomicReference<Throwable> exception = new AtomicReference<>();

    /**
     * Compiler output stream for capturing compile errors
     */
    @Nonnull
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Result of the last script
     */
    @Nonnull
    private final AtomicReference<Object> result = new AtomicReference<>();
    /**
     * Map of variable names to values for bindings
     */
    @Nonnull
    private final Map<String, Object> values = Maps.newHashMap();
    /**
     * Spark context
     */
    @Nullable
    private SparkContext sparkContext;
    /**
     * Spark SQL context
     */
    @Nullable
    private SQLContext sqlContext;

    /**
     * Executes the specified script.
     *
     * @param script the script to be executed
     * @return the value returned from the script
     * @throws ScriptException if an error occurs in the script
     */
    @Nullable
    public synchronized Object eval(@Nonnull final String script) throws ScriptException {
        List<NamedParam> bindings = ImmutableList.of();
        return eval(script, bindings);
    }

    /**
     * Executes the specified script with the given bindings.
     *
     * @param script   the script to be executed
     * @param bindings the variable bindings to be accessible to the script
     * @return the value returned from the script
     * @throws ScriptException if an error occurs in the script
     */
    @Nullable
    public synchronized Object eval(@Nonnull final String script, @Nonnull final List<NamedParam> bindings)
        throws ScriptException {
        // Define class containing script
        final StringBuilder cls = new StringBuilder();
        cls.append("class Script (engine: com.thinkbiganalytics.spark.repl.ScriptEngine)");
        cls.append("    extends com.thinkbiganalytics.spark.repl.Script (engine) {\n");
        cls.append("  override def eval (): Any = {\n");
        cls.append(script);
        cls.append("  }\n");

        // Add bindings to class
        this.values.clear();

        for (NamedParam param : bindings) {
            cls.append("  def ");
            cls.append(param.name());
            cls.append(" (): ");
            cls.append(param.tpe());
            cls.append(" = getValue(\"");
            cls.append(param.name());
            cls.append("\")\n");
            this.values.put(param.name(), param.value());
        }

        cls.append("}\n");

        // Instantiate class
        cls.append("new Script(engine).run()\n");

        // Execute script
        this.out.reset();

        execute(cls.toString());

        // Check for exception and return result
        checkCompileError();
        checkRuntimeError();

        return this.result.get();
    }

    /**
     * Gets the class loader used by the interpreter.
     *
     * @return the class loader
     */
    @Nonnull
    public abstract ClassLoader getClassLoader();

    /**
     * Gets the {@code SparkContext} available to scripts as {@code sc}.
     *
     * @return the Spark context
     */
    @Nonnull
    public SparkContext getSparkContext() {
        if (this.sparkContext == null) {
            this.sparkContext = createSparkContext();
        }
        return this.sparkContext;
    }

    /**
     * Gets the {@code SQLContext} available to scripts as {@code sqlContext}.
     *
     * @return the SQL context
     */
    @Nonnull
    public SQLContext getSQLContext() {
        if (this.sqlContext == null) {
            this.sqlContext = new HiveContext(getSparkContext());
        }
        return this.sqlContext;
    }

    /**
     * Creates the {@code SparkContext} that will be available to scripts as {@code sc}.
     *
     * @return the Spark context
     */
    @Nonnull
    protected abstract SparkContext createSparkContext();

    /**
     * Executes the specified script.
     *
     * @param script the script to be executed
     * @throws ScriptException if an error occurs in the script
     */
    protected abstract void execute(@Nonnull final String script) throws ScriptException;

    /**
     * Gets the writer for capturing compile errors.
     *
     * @return the compiler output stream
     */
    protected PrintWriter getPrintWriter() {
        return new PrintWriter(this.out);
    }

    /**
     * Resets the engine state so the {@link SparkContext} can be recreated.
     */
    protected void reset() {
        // Stop Spark
        if (sparkContext != null && !sparkContext.isStopped()) {
            sparkContext.stop();
        }

        // Clear instance variables
        exception.set(null);
        out.reset();
        result.set(null);
        sparkContext = null;
        sqlContext = null;
    }

    /**
     * Gets the value of the specified binding.
     *
     * @param name the name of the binding
     * @return the value of the binding
     */
    @Nullable
    Object getValue(@Nonnull final String name) {
        return this.values.get(name);
    }

    /**
     * Sets the runtime exception for the current script.
     *
     * @param t the exception
     */
    void setException(@Nonnull final Throwable t) {
        this.exception.set(t);
    }

    /**
     * Sets the result of the current script.
     *
     * @param result the result
     */
    void setResult(@Nullable final Object result) {
        this.exception.set(null);
        this.result.set(result);
    }

    /**
     * Checks the output stream for a compile error.
     *
     * @throws ScriptException if a compile error is found
     */
    private void checkCompileError() throws ScriptException {
        byte[] outBytes = this.out.toByteArray();

        // Look for label
        int labelIndex = ArrayUtils.indexOf(outBytes, 0, outBytes.length, LABEL, 0, LABEL.length, 0);
        if (labelIndex == -1) {
            return;
        }

        // Look for start and end of message
        int lineIndex = labelIndex + LABEL.length + 1;
        int msgStart = ArrayUtils.indexOf(outBytes, 0, outBytes.length, SEPARATOR, 0, SEPARATOR.length, lineIndex) + 2;
        int msgEnd = ArrayUtils.indexOf(outBytes, 0, outBytes.length, END_LINE, 0, END_LINE.length, msgStart);

        // Throw exception
        int line;
        String message;

        try {
            line = Integer.parseInt(new String(outBytes, lineIndex, msgStart - lineIndex - 2));
            message = new String(outBytes, msgStart, msgEnd - msgStart, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        throw new ScriptException(message, "<console>", line);
    }

    /**
     * Checks for a runtime exception.
     *
     * @throws ScriptException if an exception is found
     */
    private void checkRuntimeError() throws ScriptException {
        Throwable exception = this.exception.get();

        if (exception != null) {
            Throwables.propagateIfPossible(exception, ScriptException.class);

            if (exception instanceof Exception) {
                throw new ScriptException((Exception) exception);
            } else {
                throw new ScriptException(exception.getMessage());
            }
        }
    }
}
