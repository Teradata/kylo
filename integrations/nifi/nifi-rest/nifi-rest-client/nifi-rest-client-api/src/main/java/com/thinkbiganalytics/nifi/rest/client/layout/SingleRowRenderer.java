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

/**
 * Render items in a row horizontally
 */
public class SingleRowRenderer extends AbstractRenderer {

    private Double yValue;

    public SingleRowRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig, Double yValue) {
        super(layoutGroup, alignmentConfig);
        this.yValue = yValue;
    }

    private void storePosition(Location location, PositionDTO positionDTO) {
        storePosition(location.name(), positionDTO);
    }

    private PositionDTO getLastPosition(Location location) {
        return getLastLocationPositions().get(location.name());
    }

    @Override
    public PositionDTO getNextPosition(PositionDTO lastPosition) {

        PositionDTO newPosition = new PositionDTO();
        newPosition.setX(alignmentConfig.getCenterX() - (alignmentConfig.getProcessGroupWidth() / 2));
        newPosition.setY(yValue);

        Location currentLocation = null;
        Location lastLocation = getLastLocationKey() != null ? Location.valueOf(getLastLocationKey()) : null;

        if (lastLocation == null) {
            currentLocation = Location.CENTER;
            storePosition(currentLocation, newPosition);
            //newPosition.setX(alignmentConfig.getCenterX());
            storePosition(Location.CENTER, newPosition);
        } else {
            if (lastLocation.equals(Location.LEFT)) {
                currentLocation = Location.RIGHT;
                //place right
                PositionDTO rightPosition = getLastPosition(Location.RIGHT);
                if (rightPosition == null) {
                    rightPosition = getLastPosition(Location.CENTER);
                }
                newPosition.setX((rightPosition != null ? rightPosition.getX() : 0) + alignmentConfig.getProcessGroupWidth() + alignmentConfig.getProcessGroupPaddingLeftRight());
                storePosition(Location.RIGHT, newPosition);
            } else {
                currentLocation = Location.LEFT;
                //place left
                PositionDTO leftPosition = getLastPosition(Location.LEFT);
                if (leftPosition == null) {
                    leftPosition = getLastPosition(Location.CENTER);
                }
                newPosition.setX((leftPosition != null ? leftPosition.getX() : 0) - (alignmentConfig.getProcessGroupWidth() + alignmentConfig.getProcessGroupPaddingLeftRight()));
                storePosition(Location.LEFT, newPosition);
            }

        }
        return newPosition;
    }

    enum Location {
        CENTER, LEFT, RIGHT;
    }

}
