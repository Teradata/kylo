package com.thinkbiganalytics.metadata.jpa.support;
/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobExecution;

import java.util.List;

public class JobStatusDslQueryExpressionBuilder {

    private static QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

    public static StringExpression jobState() {
        List<BatchJobExecution.JobStatus> runningStatus = ImmutableList.of(BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);
        CaseBuilder.Cases<String, StringExpression> jobStateCase = new CaseBuilder().when(jobExecution.status.in(runningStatus)).then(BatchJobExecution.RUNNING_DISPLAY_STATUS);
        for (BatchJobExecution.JobStatus stat : BatchJobExecution.JobStatus.values()) {
            if (stat != BatchJobExecution.JobStatus.STARTING && stat != BatchJobExecution.JobStatus.STARTED && stat != BatchJobExecution.JobStatus.UNKNOWN) {
                jobStateCase.when(jobExecution.status.eq(stat)).then(stat.name());
            }
        }
        return jobStateCase.otherwise(BatchJobExecution.JobStatus.UNKNOWN.name());
    }
}
