package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.PositionDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 11/9/16.
 */
public abstract class AbstractRenderer {

    protected LayoutGroup layoutGroup;
    protected AlignComponentsConfig alignmentConfig;
    private Integer storedPositionCounter = 0;

    /**
     * Store the last location position
     */
    private Map<String, PositionDTO> lastLocationPositions = new HashMap<>();

    private String lastLocationKey;

    public AbstractRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig) {
        this.layoutGroup = layoutGroup;
        this.alignmentConfig = alignmentConfig;
    }

    public void updateHeight(Integer height) {
        layoutGroup.setHeight(height);
        layoutGroup.setTopAndBottom(layoutGroup.getTopY(), new Double(layoutGroup.getHeight() + layoutGroup.getTopY()));
    }

    public abstract PositionDTO getNextPosition(PositionDTO lastPosition);


    public void storePosition(String locationKey, PositionDTO positionDTO) {
        lastLocationPositions.put(locationKey, positionDTO);
        lastLocationKey = locationKey;
        storedPositionCounter++;
    }

    public Map<String, PositionDTO> getLastLocationPositions() {
        return lastLocationPositions;
    }

    public String getLastLocationKey() {
        return lastLocationKey;
    }

    public PositionDTO getLastPosition() {
        if (getLastLocationKey() != null) {
            return lastLocationPositions.get(getLastLocationKey());
        }
        return null;
    }

    public Integer getStoredPositionCounter() {
        return storedPositionCounter;
    }
}