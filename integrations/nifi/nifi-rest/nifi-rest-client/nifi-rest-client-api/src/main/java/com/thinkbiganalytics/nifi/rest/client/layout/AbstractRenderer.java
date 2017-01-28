package com.thinkbiganalytics.nifi.rest.client.layout;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.PositionDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to rearrange NiFi processors and components on the canvas
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
