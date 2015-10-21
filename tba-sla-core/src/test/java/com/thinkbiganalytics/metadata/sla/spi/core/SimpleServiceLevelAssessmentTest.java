package com.thinkbiganalytics.metadata.sla.spi.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimpleServiceLevelAssessmentTest {
    
    private InMemorySLAProvider provider;
    
    private SimpleServiceLevelAssessor assessor;

    @Before
    public void setUp() throws Exception {
        this.provider = new InMemorySLAProvider();
    }


    @Test
    public void test() {
//        fail("Not yet implemented");
    }
}
