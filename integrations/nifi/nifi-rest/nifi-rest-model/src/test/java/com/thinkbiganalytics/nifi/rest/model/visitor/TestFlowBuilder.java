package com.thinkbiganalytics.nifi.rest.model.visitor;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/13/16.
 */
public class TestFlowBuilder {

    private AtomicLong groupCounter = new AtomicLong(0L);

    private AtomicLong processorCounter = new AtomicLong(0L);


    private ProcessGroupDTO processGroupDTO() {
        ProcessGroupDTO groupDTO = new ProcessGroupDTO();
        groupDTO.setName("Group " + groupCounter.incrementAndGet());
        groupDTO.setId(UUID.randomUUID().toString());
        return groupDTO;
    }

    private ProcessorDTO processorDTO() {
        ProcessorDTO processorDTO = new ProcessorDTO();
        processorDTO.setName("Processor " + processorCounter.incrementAndGet());
        processorDTO.setId(UUID.randomUUID().toString());
        return processorDTO;
    }

    private NifiVisitableProcessor processor() {
        return new NifiVisitableProcessor(processorDTO());
    }

    private void connect(NifiVisitableProcessor source, NifiVisitableProcessor dest) {
        dest.addSource(source);
        source.addDestination(dest);
    }

    @Test
    public void testFlowBuilder() {

        //build a graph of processors

        /*

                          p1
                          p2
                       p3    p4
                                p5
                                   p6


         */

        NifiVisitableProcessGroup parent = new NifiVisitableProcessGroup(processGroupDTO());
        NifiVisitableProcessor processor1 = processor();
        NifiVisitableProcessor processor2 = processor();
        NifiVisitableProcessor processor3 = processor();
        NifiVisitableProcessor processor4 = processor();
        NifiVisitableProcessor processor5 = processor();
        NifiVisitableProcessor processor6 = processor();

        //connect parent to p2
        connect(processor1, processor2);
        connect(processor2, processor3);
        connect(processor2, processor4);
        connect(processor4, processor5);
        connect(processor5, processor6);

        parent.getStartingProcessors().add(processor1);
        NifiFlowBuilder builder = new NifiFlowBuilder();
        // builder.setIdOnlyGraph(true);
        NifiFlowProcessGroup group = builder.build(parent);
        int i = 0;
    }
}
