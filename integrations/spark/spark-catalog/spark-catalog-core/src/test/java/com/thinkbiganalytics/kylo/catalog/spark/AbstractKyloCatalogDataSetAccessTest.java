package com.thinkbiganalytics.kylo.catalog.spark;

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

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import scala.Tuple2$;
import scala.collection.Map$;
import scala.collection.Seq$;

public class AbstractKyloCatalogDataSetAccessTest {

    /**
     * Verify adding files in a Java {@code List}.
     */
    @Test
    public void addFilesWithList() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.addFiles(Arrays.asList("path1", "path2"));
        Mockito.verify(access).addFile("path1");
        Mockito.verify(access).addFile("path2");
    }

    /**
     * Verify adding files in a Scala {@code Seq}.
     */
    @Test
    public void addFilesWithSeq() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.addFiles(Seq$.MODULE$.newBuilder().$plus$eq("path1").$plus$eq("path2").result());
        Mockito.verify(access).addFile("path1");
        Mockito.verify(access).addFile("path2");
    }

    /**
     * Verify adding jars in a Scala {@code Seq}.
     */
    @Test
    public void addJarsWithSeq() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.addJars(Seq$.MODULE$.newBuilder().$plus$eq("path1").$plus$eq("path2").result());
        Mockito.verify(access).addJars(Arrays.asList("path1", "path2"));
    }

    /**
     * Verify adding an option with a {@code boolean} value.
     */
    @Test
    public void optionWithBoolean() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.option("key", true);
        Mockito.verify(access).option("key", "true");
    }

    /**
     * Verify adding an option with a {@code double} value.
     */
    @Test
    public void optionWithDouble() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.option("key", 3.14);
        Mockito.verify(access).option("key", "3.14");
    }

    /**
     * Verify adding an option with a {@code long} value.
     */
    @Test
    public void optionWithLong() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.option("key", 42L);
        Mockito.verify(access).option("key", "42");
    }

    /**
     * Verify adding options from a Java {@code Map}.
     */
    @Test
    public void optionsWithJavaMap() {
        final Map<String, String> options = new HashMap<>();
        options.put("key1", "value1");
        options.put("key2", "value2");

        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.options(options);
        Mockito.verify(access).option("key1", "value1");
        Mockito.verify(access).option("key2", "value2");
    }

    /**
     * Verify adding options from a Scala {@code Map}.
     */
    @Test
    public void optionsWithScalaMap() {
        final AbstractKyloCatalogDataSetAccess access = Mockito.spy(AbstractKyloCatalogDataSetAccess.class);
        access.options(Map$.MODULE$.newBuilder().$plus$eq(Tuple2$.MODULE$.<Object, Object>apply("key1", "value1")).$plus$eq(Tuple2$.MODULE$.<Object, Object>apply("key2", "value2")).result());
        Mockito.verify(access).option("key1", "value1");
        Mockito.verify(access).option("key2", "value2");
    }
}
