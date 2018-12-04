package com.thinkbiganalytics.spark.scala;

/*-
 * #%L
 * kylo-commons-spark-shell-plugin-shared
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


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import static org.assertj.core.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ScalaImportManagerTest.Config.class},
                      loader = AnnotationConfigContextLoader.class)
public class ScalaImportManagerTest {
    private static Map<String,String> testImports = ImmutableMap.of("import org.apache.spark.sql._",
                                                                    "import org.apache.spark.sql._",
                                                                    "import org.apache.spark.sql.functions.\\{[^}]+\\}",
                                                                    "import org.apache.spark.sql.functions._",
                                                                    "import com.thinkbiganalytics.kylo.catalog._",
                                                                    "import com.thinkbiganalytics.kylo.catalog._"
    );

    @Resource
    private ScalaImportManager scalaImportManager;

    @Test
    public void getManagedImports() {
        Set<String> actualImps = scalaImportManager.getManagedImports();
        assertThat(actualImps).isEqualTo(Sets.newHashSet(testImports.values()));
    }

    @Test
    public void stripImports() {
        String script = "import org.apache.spark.sql._\n"
                          + "import org.apache.spark.sql.functions.{concat, lit, concat_ws,collect_list,split,size,col,when}\n"
                          +"import com.thinkbiganalytics.kylo.catalog._\n"
                          + "print 1";

        String actualScript = scalaImportManager.stripImports(script);
        assertThat(actualScript).isEqualToIgnoringWhitespace("print 1");
    }

    @Configuration
    static class Config {

        @Bean
        public ScalaImportManager scalaImportManager() {
            return new ScalaImportManager(testImports);
        }
    }
}
