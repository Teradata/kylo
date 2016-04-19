package com.thinkbiganalytics.nifi.rest.model.visitor;

import org.apache.nifi.web.api.dto.ConnectionDTO;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableConnection implements  NifiVisitable{

    private ConnectionDTO dto;

    public NifiVisitableConnection(ConnectionDTO dto ){
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
}
