package com.thinkbiganalytics.metadata.modeshape.common;

import javax.jcr.Node;

/**
 * Common object to hold an arbitary map of properties
 */
public class JcrProperties extends JcrObject {

    public static String NODE_TYPE = "tba:properties";

   public JcrProperties(Node node) {
        super(node);
    }



}
