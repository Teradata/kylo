package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.PositionDTO;

/**
 * Created by sr186054 on 11/9/16.
 */
public class ColumnRenderer extends AbstractRenderer {

    private String LOCATION_KEY = "Column";


    private Double xValue;

    private Integer itemCount;


    public ColumnRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig, Double xValue, Integer itemCount) {
        super(layoutGroup, alignmentConfig);
        this.xValue = xValue;
        this.itemCount = itemCount;
    }


    @Override
    public PositionDTO getNextPosition(PositionDTO lastPosition) {

        Double
            yValue =
            getLastPosition() == null ? layoutGroup.getTopY()
                                      : getLastPosition().getY() + (layoutGroup.getHeight() / itemCount);
        //force the last one to go the the bottom
        if (getStoredPositionCounter() == (itemCount - 1)) {
            yValue = layoutGroup.getBottomY();
        }
        PositionDTO newPosition = new PositionDTO();
        newPosition.setX(xValue);
        newPosition.setY(yValue);
        storePosition(LOCATION_KEY, newPosition);
        return newPosition;
    }


    public Integer getItemCount() {
        return itemCount;
    }

    public Double getxValue() {
        return xValue;
    }
}

