package com.thinkbiganalytics.metadata.modeshape.common;

/**
 * Listener interface that is called via the ModeShapeAvailability class
 *
 * @see ModeShapeAvailability
 */
public interface ModeShapeAvailabilityListener {

    /**
     * Called when Modeshape is available
     */
    void modeShapeAvailable();

}
