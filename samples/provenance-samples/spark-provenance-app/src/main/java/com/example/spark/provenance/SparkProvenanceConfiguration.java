package com.example.spark.provenance;

/*-
 * #%L
 * kylo-spark-provenance-app
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


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Defines acceptable command line parameters for Validator application.
 */
public class SparkProvenanceConfiguration {


    enum Type {
        JMS, KAFKA
    }

    /**
     * the UUID for the current flow file.
     */
    private  String flowFileId;
    /**
     * the UUID for the job flow file.  This is the flow file uuid for the processor/event that originated the NiFi flow.
     */
    private  String jobFlowFileId;
    /**
     * The name of the feed (category.feed)
     */
    private String feedName;
    /**
     * The URL to connect to either JMS(tcp:61616) or KAFKA (sandbox.kylo.io:6667)
     */
    private String connectionUrl;
    /**
     * Can be JMS, or KAFKA.
     * If the Spark job is running in the Cluster KAFKA should be used.
     * If the Spark job is running locally then JMS can be used
     */
    private String type;

    public SparkProvenanceConfiguration(@Nonnull final String[] args) {
        flowFileId = args[0];
        jobFlowFileId = args[1];
        feedName = args[2];
        connectionUrl = args[3];
        type = args[4];
    }

    public String getFeedName() {
        return feedName;
    }

    public String getFlowFileId() {
        return flowFileId;
    }

    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Type getType() {
        try {
            return Type.valueOf(type);
        }catch (Exception e){
            return deriveType();
        }
    }

    private Type deriveType(){
        if(connectionUrl.startsWith("tcp")){
            return Type.JMS;
        }
        else {
            return Type.KAFKA;
        }
    }
}
