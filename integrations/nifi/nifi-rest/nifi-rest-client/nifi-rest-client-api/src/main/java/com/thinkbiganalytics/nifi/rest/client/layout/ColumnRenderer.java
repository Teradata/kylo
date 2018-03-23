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
 * Render a group of items in a column vertically
 */
public class ColumnRenderer extends AbstractRenderer {

    public static String LOCATION_KEY = "Column";


    private Double xValue;

    private Integer itemCount;

    private Integer height;

    private boolean alignLastToBottom = true;


    public ColumnRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig, Double xValue, Integer itemCount) {
        super(layoutGroup, alignmentConfig);
        this.xValue = xValue;
        this.itemCount = itemCount;
        this.height = layoutGroup.getHeight();
    }

    private Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setAlignLastToBottom(boolean alignLastToBottom) {
        this.alignLastToBottom = alignLastToBottom;
    }

    @Override
    public PositionDTO getNextPosition(PositionDTO lastPosition) {

        Double
            yValue =
            getLastPosition() == null ? layoutGroup.getTopY()
                                      : getLastPosition().getY() + (getHeight() / itemCount);
        //force the last one to go the the bottom
        if (alignLastToBottom && getStoredPositionCounter() == (itemCount - 1)) {
            yValue = layoutGroup.getBottomY();
        }
        PositionDTO newPosition = new PositionDTO();
        newPosition.setX(xValue);
        newPosition.setY(yValue);
        storePosition(LOCATION_KEY, newPosition);
        return newPosition;
    }

    public void storePosition(PositionDTO positionDTO) {
        storePosition(LOCATION_KEY, positionDTO);
    }

    public void storePosition(Double x, Double y) {
        PositionDTO positionDTO = getLastLocationPositions().get(LOCATION_KEY);
        if (positionDTO == null) {
            positionDTO = new PositionDTO();
        }
        positionDTO.setX(x);
        positionDTO.setY(y);
        storePosition(positionDTO);

    }

    public Integer getItemCount() {
        return itemCount;
    }

    public Double getxValue() {
        return xValue;
    }
}

