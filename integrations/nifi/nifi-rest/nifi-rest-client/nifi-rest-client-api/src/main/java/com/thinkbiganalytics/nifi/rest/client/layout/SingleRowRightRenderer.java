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
public class SingleRowRightRenderer extends AbstractRenderer {

    private Double yValue;

    private Integer xSpace;

    public SingleRowRightRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig, Double yValue) {
        super(layoutGroup, alignmentConfig);
        this.yValue = yValue;
    }

    public SingleRowRightRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig, Double yValue, Integer xSpace) {
        super(layoutGroup, alignmentConfig);
        this.yValue = yValue;
        this.xSpace = xSpace;

    }

    public void storePosition(PositionDTO positionDTO) {
        storePosition(Location.RIGHT.name(), positionDTO);
    }

    public PositionDTO getLastPosition() {
        return getLastLocationPositions().get(Location.RIGHT.name());
    }

    @Override
    public PositionDTO getNextPosition(PositionDTO lastPosition) {

        if (this.xSpace == null) {
            this.xSpace = alignmentConfig.getProcessGroupWidth() + alignmentConfig.getProcessGroupPaddingLeftRight();
        }

        PositionDTO newPosition = new PositionDTO();
        newPosition.setX(alignmentConfig.getCenterX() - (alignmentConfig.getProcessGroupWidth() / 2));
        newPosition.setY(yValue);

        if (lastPosition == null) {
            storePosition(newPosition);
        } else {
            //place right
            PositionDTO rightPosition = getLastPosition();
            newPosition.setX((rightPosition != null ? rightPosition.getX() : 0) + xSpace);
            storePosition(newPosition);
        }
        return newPosition;
    }

    enum Location {
        RIGHT;
    }

}
