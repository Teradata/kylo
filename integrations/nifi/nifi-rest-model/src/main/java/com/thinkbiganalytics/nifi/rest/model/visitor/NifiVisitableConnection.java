package com.thinkbiganalytics.nifi.rest.model.visitor;

import org.apache.nifi.web.api.dto.ConnectionDTO;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableConnection implements  NifiVisitable{

    private ConnectionDTO dto;
    private NifiVisitableProcessGroup group;

    public NifiVisitableConnection(NifiVisitableProcessGroup group,ConnectionDTO dto ){
        this.group = group;
        this.dto = dto;
    }

    //"INPUT_PORT","OUTPUT_PORT

    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {

        nifiVisitor.visitConnection(this);
        if(dto.getDestination() !=null) {

        }
    }

    public ConnectionDTO getDto() {
        return dto;
    }

    public NifiVisitableProcessGroup getGroup() {
        return group;
    }
}
