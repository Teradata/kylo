package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
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


import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

public class ScriptGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ScriptGenerator.class);

    @Resource
    private Map<String, String> scriptRegistry;

    public String script(String name, Object... args) {
        Validate.isTrue(scriptRegistry.containsKey(name),
                String.format("Unable to find a script with name = '%s'", name));

        // TODO: store in a dedent'ing map, rather than trim here
        String script = String.format(scriptRegistry.get(name).trim(), args);
        logger.debug("Constructed script with name='{}' as '{}'", name, script);

        return script;
    }


    public String wrappedScript(String name, String pre, String post, Object... args) {
        Validate.isTrue(scriptRegistry.containsKey(name),
                String.format("Unable to find a script with name = '%s'", name));

        // nulls ok
        pre = pre == null ? "" : pre;
        post = post == null ? "" : post;

        // TODO: store in a dedent'ing map, rather than trim here
        String script = new StringBuilder(pre)
            .append(String.format(scriptRegistry.get(name).trim(), args))
            .append(post).toString();

        logger.debug("Constructed script with name='{}' as '{}'", name, script);

        return script;
    }
}
