package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.PositionDTO;

/**
 * Created by sr186054 on 11/9/16.
 */
public class TopBottomRowsRenderer extends AbstractRenderer {

    enum Location {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
        private Location next;

        static {
            TOP_LEFT.next = TOP_RIGHT;
            TOP_RIGHT.next = BOTTOM_LEFT;
            BOTTOM_LEFT.next = BOTTOM_RIGHT;
            BOTTOM_RIGHT.next = TOP_LEFT;
        }

        public Location getNext() {
            return next;
        }
    }

    Double centerX;


    public TopBottomRowsRenderer(LayoutGroup layoutGroup, AlignComponentsConfig alignmentConfig) {
        super(layoutGroup, alignmentConfig);
        updateHeight((alignmentConfig.getProcessGroupHeight() + alignmentConfig.getProcessGroupPaddingTopBottom()) * 2);
    }

    @Override
    public void updateHeight(Integer height) {
        super.updateHeight(height);
        this.centerX = alignmentConfig.getCenterX() - (alignmentConfig.getProcessGroupWidth() / 2);
    }

    private void storePosition(Location location, PositionDTO positionDTO) {
        storePosition(location.name(), positionDTO);
    }

    private Double topYValue() {
        return layoutGroup.getTopY();
    }

    private Double bottomYValue() {
        return layoutGroup.getBottomY();
    }

    private PositionDTO getInitialPosition(Location location) {
        Double groupPlusPadding = new Double(alignmentConfig.getProcessGroupWidth() + alignmentConfig.getProcessGroupPaddingLeftRight());
        PositionDTO dto = new PositionDTO();
        dto.setX(centerX + groupPlusPadding);
        dto.setY(layoutGroup.getBottomY());
        switch (location) {
            case BOTTOM_RIGHT:
                dto.setX(centerX + groupPlusPadding);
                dto.setY(layoutGroup.getBottomY());
                break;
            case BOTTOM_LEFT:
                dto.setX(centerX - groupPlusPadding);
                dto.setY(layoutGroup.getBottomY());
                break;
            case TOP_RIGHT:
                dto.setX(centerX + groupPlusPadding);
                dto.setY(layoutGroup.getTopY());
                break;
            case TOP_LEFT:
                dto.setX(centerX - groupPlusPadding);
                dto.setY(layoutGroup.getTopY());
                break;

            default:
                break;

        }
        return dto;
    }

    private PositionDTO getNextPosition(Location location, PositionDTO lastPosition) {
        Double groupPlusPadding = new Double(alignmentConfig.getProcessGroupWidth() + alignmentConfig.getProcessGroupPaddingLeftRight());
        Double padding = new Double(alignmentConfig.getProcessGroupPaddingLeftRight());
        PositionDTO dto = new PositionDTO();
        dto.setX(centerX + groupPlusPadding);
        dto.setY(layoutGroup.getBottomY());
        switch (location) {
            case BOTTOM_RIGHT:
                dto.setX(lastPosition.getX() + groupPlusPadding);
                dto.setY(layoutGroup.getBottomY());
                break;
            case BOTTOM_LEFT:
                dto.setX(lastPosition.getX() - groupPlusPadding);
                dto.setY(layoutGroup.getBottomY());
                break;
            case TOP_RIGHT:
                dto.setX(lastPosition.getX() + groupPlusPadding);
                dto.setY(layoutGroup.getTopY());
                break;
            case TOP_LEFT:
                dto.setX(lastPosition.getX() - groupPlusPadding);
                dto.setY(layoutGroup.getTopY());
                break;
            default:
                break;

        }
        return dto;
    }

    private PositionDTO getLastPosition(Location location) {
        return getLastLocationPositions().get(location.name());
    }

    @Override
    public PositionDTO getNextPosition(PositionDTO lastPosition) {

        PositionDTO newPosition = null;

        Location currentLocation = null;
        Location lastLocation = getLastLocationKey() != null ? Location.valueOf(getLastLocationKey()) : null;

        if (lastLocation == null) {
            currentLocation = Location.TOP_LEFT;
        } else {
            currentLocation = lastLocation.getNext();
        }

        PositionDTO lastPositionAtLocation = getLastPosition(currentLocation);
        if (lastPositionAtLocation != null) {
            newPosition = getNextPosition(currentLocation, lastPositionAtLocation);
        } else {
            newPosition = getInitialPosition(currentLocation);
        }
        storePosition(currentLocation, newPosition);
        return newPosition;
    }

}