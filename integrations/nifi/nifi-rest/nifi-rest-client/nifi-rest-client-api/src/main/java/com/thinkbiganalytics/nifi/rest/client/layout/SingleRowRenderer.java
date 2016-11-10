package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.PositionDTO;

/**
 * Created by sr186054 on 11/9/16.
 */
public class SingleRowRenderer extends AbstractRenderer {

    enum Location {
        CENTER, LEFT, RIGHT;
    }

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

}