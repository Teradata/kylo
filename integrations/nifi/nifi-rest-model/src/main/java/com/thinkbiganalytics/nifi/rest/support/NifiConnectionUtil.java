package com.thinkbiganalytics.nifi.rest.support;

import org.apache.nifi.web.api.dto.ConnectionDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 1/14/16.
 */
public class NifiConnectionUtil {

    public static List<String> getInputProcessorIds(Collection<ConnectionDTO> connections) {
        List<String> processorIds = new ArrayList<>();
        SourcesAndDestinations connectionItems = new SourcesAndDestinations(connections);
        //find all sources that are not in a destination
        for(String source : connectionItems.getSourceConnections()){
            if(!connectionItems.getDestinationConnections().contains(source)){
                processorIds.add(source);
            }
        }
        return processorIds;
    }
    private static class SourcesAndDestinations{

       private List<String> destinationConnections = new ArrayList<>();
        private List<String> sourceConnections = new ArrayList<>();

        public SourcesAndDestinations(Collection<ConnectionDTO> connections){
        if(connections != null) {
            for(ConnectionDTO connection : connections){
                sourceConnections.add(connection.getSource().getId());
                destinationConnections.add(connection.getDestination().getId());
            }
        }
        }

        public List<String> getDestinationConnections() {
            return destinationConnections;
        }

        public List<String> getSourceConnections() {
            return sourceConnections;
        }
    }

    public static List<String> getEndingProcessorIds(Collection<ConnectionDTO> connections) {
        List<String> processorIds = new ArrayList<>();
        SourcesAndDestinations connectionItems = new SourcesAndDestinations(connections);


        //find all destinations that are not in a source

        for(String dest : connectionItems.getDestinationConnections()){
            if(!connectionItems.getSourceConnections().contains(dest)){
                processorIds.add(dest);
            }
        }
        return processorIds;
    }
}
