package com.thinkbiganalytics.kylo.spark.ingest;

/*-
 * #%L
 * Kylo Spark Ingest App
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

import com.beust.jcommander.converters.IParameterSplitter;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Converts a list of values into a Java {@link List}.
 */
public class SemicolonParameterSplitter implements IParameterSplitter {

    @Nonnull
    @Override
    public List<String> split(@Nonnull final String value) {
        return Arrays.asList(value.split(";"));
    }
}
