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


import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;


public class ScalaImportManager {
    // a Map with values that will be imported at session startup, and keys that are an equivalent import that can be removed from transforms
    private Map<String, String> managedImports;

    private Collection<Pattern> removerPatterns;

    public ScalaImportManager(Map<String, String> managedImports) {
        this.managedImports = ImmutableMap.copyOf(managedImports);
        this.removerPatterns = getRemoverPatterns(managedImports);
    }


    public Set<String> getManagedImports() {
        return Sets.newHashSet(managedImports.values());
    }

    public String stripImports(String input) {
        String stripped = input;
        for( Pattern remover : removerPatterns ) {
            Matcher m = remover.matcher(stripped);
            stripped = m.replaceAll("");
        }
        return stripped;
    }

    private Collection<Pattern> getRemoverPatterns(Map<String,String> managedImports) {
        return Collections2.transform(managedImports.keySet(), new Function<String, Pattern>() {
            @Nullable
            @Override
            public Pattern apply(@Nullable String input) {
                return Pattern.compile("^\\s*" + input + "\\s*$", Pattern.MULTILINE);
            }
        });
    }

    // remove form string
}
