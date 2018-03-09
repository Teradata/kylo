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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventDtoBuilder;
import com.thinkbiganalytics.provenance.api.ProvenanceEventService;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.hive.HiveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * An Example Spark Job that generates some extra Provenance data
 * This will publish the custom events to either JMS or KAFKA based upon the supplied Configuration
 * @see SparkProvenanceConfiguration
 */
public class SparkProvenance {

    private static final Logger log = LoggerFactory.getLogger(SparkProvenance.class);

    public static void main(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark Provenance with the following command line args (comma separated):{}", StringUtils.join(args, ","));
        }
        new SparkProvenance().run(System.out, args);
    }

    private void run(@Nonnull final PrintStream out, @Nonnull final String... args) {
        // Check how many arguments were passed in
        if (args.length < 5) {
            String msg = "Proper Usage is: <flowfile-id> <job-flowfile-id> <feed-name (category.feed)> <connection-url (url to connect to JMS or KAFAK)> <type (JMS, KAFKA)>" +
                         "You provided " + args.length + " args which are (comma separated): " + StringUtils.join(args, ",");
            out.println(msg);
            throw new IllegalArgumentException(msg);
        }

        ProvenanceEventService provenanceEventService = null;
        final SparkContext sparkContext = SparkContext.getOrCreate();

        try {
            final SparkProvenanceConfiguration params = new SparkProvenanceConfiguration(args);

            //Get the proper ProvenanceService
            provenanceEventService = ProvenanceServiceFactory.getProvenanceEventService(params);

            //Collection of custom Provenance Events we will be sending to Kylo
            List<ProvenanceEventRecordDTO> events = new ArrayList<>();

            //do some work.  Look up the database names in Hive
            final HiveContext hiveContext = new HiveContext(sparkContext);

            //Do some work... i.e. look up the Databases in Hive
            ProvenanceEventRecordDTO event = newEvent("Databases",params);
            Dataset df = hiveContext.sql("show databases");
            event.getAttributeMap().put("databases", df.toJSON().collectAsList().toString());
            event.setEventTime(System.currentTimeMillis());
            events.add(event);

            event = newEvent("Another Step",params);
            event.getAttributeMap().put("UUID 1", UUID.randomUUID().toString());
            event.setEventTime(System.currentTimeMillis());
            event.getAttributeMap().put("timestamp", String.valueOf(System.currentTimeMillis()));
            events.add(event);

            //Send the events off
            provenanceEventService.sendEvents(events);

            log.info("Spark app finished");
        } catch (Exception e) {
            log.error("Failed to run Spark Provenance Job: {}", e.toString(), e);

        } finally {
            provenanceEventService.closeConnection();
            sparkContext.stop();
            log.info("Exiting!!!!");
            System.exit(0);

        }
    }

    /**
     * Build a new Provenance Event
     * @param componentName
     * @param startingEvent
     * @param params
     * @return
     */
    private ProvenanceEventRecordDTO newEvent(String componentName,SparkProvenanceConfiguration params){

        return new ProvenanceEventDtoBuilder(params.getFeedName(),params.getFlowFileId(),componentName)
            .jobFlowFileId(params.getJobFlowFileId())
            .startTime(System.currentTimeMillis())
            .startingEvent(false)
            .build();
    }




}
