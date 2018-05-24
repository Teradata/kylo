package com.thinkbiganalytics.kylo.catalog.spark;

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

import com.thinkbiganalytics.kylo.hadoop.FileSystemUtil;
import com.thinkbiganalytics.kylo.protocol.hadoop.Handler;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import scala.Option;

public class SparkUtilTest {

    /**
     * Verify getting the value of an option.
     */
    @Test
    public void getOrElse() {
        Assert.assertEquals("optionValue", SparkUtil.getOrElse(Option.apply("optionValue"), "defaultValue"));
        Assert.assertEquals("defaultValue", SparkUtil.getOrElse(Option.<String>empty(), "defaultValue"));
    }

    /**
     * Verify parsing URLs from strings.
     */
    @Test
    public void parseUrl() throws MalformedURLException {
        Handler.register();
        Assert.assertEquals(new URL("file:/path/file.ext"), SparkUtil.parseUrl("/path/file.ext"));
        Assert.assertEquals(new URL("file:/path/file.ext"), SparkUtil.parseUrl("file:/path/file.ext"));
        Assert.assertEquals(new URL("file:/path/file.ext"), SparkUtil.parseUrl("local:/path/file.ext"));
        Assert.assertEquals(new URL("hadoop:hdfs://localhost:8020/path/file.ext"), FileSystemUtil.parseUrl("hdfs://localhost:8020/path/file.ext", null));
    }
}
