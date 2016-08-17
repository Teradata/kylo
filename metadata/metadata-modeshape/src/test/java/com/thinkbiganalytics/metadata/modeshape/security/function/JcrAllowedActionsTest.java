/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.function;

import javax.inject.Inject;

import org.springframework.boot.test.SpringApplicationConfiguration;

import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;

/**
 *
 * @author Sean Felten
 */
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, TestActionsConfig.class })
public class JcrAllowedActionsTest {

    @Inject
    private JcrAllowedActions provider;
    
    
}
