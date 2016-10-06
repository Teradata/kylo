/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

/**
 * Thrown when an attempt to load a high-water mark fails due to it being already active pending release.
 * 
 * @author Sean Felten
 */
public class WaterMarkActiveException extends Exception {

    private static final long serialVersionUID = 1L;

    public WaterMarkActiveException(String waterMarkName) {
        super(waterMarkName);
    }
}
