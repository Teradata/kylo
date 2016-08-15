package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.DelayedProvenanceEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileCache;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Created by sr186054 on 8/14/16.
 */
public class DelayedProvenanceEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DelayedProvenanceEventProducer.class);

    private StreamConfiguration configuration;

    // Creates an instance of blocking queue using the DelayQueue.
    private BlockingQueue<DelayedProvenanceEvent> queue;

    public DelayedProvenanceEventProducer(StreamConfiguration configuration, BlockingQueue queue) {
        super();
        this.configuration = configuration;
        this.queue = queue;
    }


    private ProvenanceEventRecordDTO cache(ProvenanceEventDTO dto) {
        //convert it to a dto so we can add additional tracking properties

        log.info("CACHE Event Id ({}) , Processor Id ({}) ", dto.getEventId(), dto.getComponentId());

        ActiveFlowFile flowFile = FlowFileCache.instance().getEntry(dto.getFlowFileUuid());

        //??? is this needed?
        flowFile.addEvent(dto.getEventId());

        //create the new DTO with ref to the Flow File
        ProvenanceEventRecordDTO eventDto = new ProvenanceEventRecordDTO(dto, flowFile);

        //ref to the starting flowfile
        ActiveFlowFile rootFlowFile = null;
        if (ProvenanceEventUtil.isFirstEvent(dto)) {
            flowFile.setFirstEvent(eventDto);
            rootFlowFile = flowFile;
            flowFile.setRootFlowFile(rootFlowFile);
        } else {
            rootFlowFile = flowFile.getRootFlowFile();
        }

        if (dto.getParentUuids() != null && !dto.getParentUuids().isEmpty()) {
            for (String parent : dto.getParentUuids()) {
                ActiveFlowFile parentFlowFile = flowFile.addParent(FlowFileCache.instance().getEntry(parent));
                parentFlowFile.addChild(flowFile);
                if (rootFlowFile != null && parentFlowFile.getRootFlowFile() == null) {
                    parentFlowFile.setRootFlowFile(rootFlowFile);
                }
            }
        }

        if (dto.getChildUuids() != null && !dto.getChildUuids().isEmpty()) {
            for (String child : dto.getChildUuids()) {
                ActiveFlowFile childFlowFile = flowFile.addParent(FlowFileCache.instance().getEntry(child));
                childFlowFile.addParent(flowFile);
                if (childFlowFile != null && childFlowFile.getRootFlowFile() == null) {
                    childFlowFile.setRootFlowFile(rootFlowFile);
                }
            }
        }
        return eventDto;
    }


    public void add(ProvenanceEventDTO event) {
        ProvenanceEventRecordDTO eventDto = cache(event);
        DelayedProvenanceEvent delayedProvenanceEvent = new DelayedProvenanceEvent(eventDto, configuration.getProcessDelay());
        queue.offer(delayedProvenanceEvent);

    }


}
