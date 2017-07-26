package com.thinkbiganalytics.spark.policy;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-api
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

import com.thinkbiganalytics.policy.FieldPoliciesJsonTransformer;
import com.thinkbiganalytics.policy.FieldPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Map;

@Component
public class FieldPolicyLoader implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FieldPolicyLoader.class);

    /**
     * read the JSON file path and return the JSON string
     *
     * @param path path to field policy JSON file
     */
    public Map<String, FieldPolicy> loadFieldPolicy(String path) {
        log.info("Loading Field Policy JSON file at {} ", path);
        String policyJson = "[]";

        /**
         * If spark is running in yarn-cluster mode, the policyJson file will be passed via --files param to be
         * added into driver classpath in Application Master. The "path" won't be valid in that case,
         * as it would be pointing to local file system. To enable this, we should be checking the fieldPolicyFile
         * in the current location ie classpath for "yarn-cluster" mode as well as the path for "yarn-client" mode

         * You can also use sparkcontext object to get the value of sparkContext.getConf().get("spark.submit.deployMode")
         * and use this to decide which readFieldPolicyJsonPath to choose.
         */
        File policyFile = new File(path);
        if (policyFile.exists() && policyFile.isFile()) {
            log.info("Loading field policies at {} ", path);
        } else {
            log.info("Couldn't find field policy file at {} will check classpath.", path);
            String fileName = policyFile.getName();
            path = "./" + fileName;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            if (line == null) {
                log.error("Field policies file at {} is empty ", path);
            }

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            policyJson = sb.toString();
        } catch (Exception e) {
            log.error("Error parsing field policy file. Please verify valid JSON at path {}", e.getMessage(), e);
        }
        FieldPoliciesJsonTransformer fieldPoliciesJsonTransformer = new FieldPoliciesJsonTransformer(policyJson);
        fieldPoliciesJsonTransformer.augmentPartitionColumnValidation();
        Map<String, FieldPolicy> map = fieldPoliciesJsonTransformer.buildPolicies();

        log.info("Finished building field policies for file: {} with entity that has {} fields ", path, map.size());
        return map;
    }

}
