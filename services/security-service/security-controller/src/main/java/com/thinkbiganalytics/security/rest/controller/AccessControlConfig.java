/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class AccessControlConfig {

//    @Bean(name="servicesAllowedActionsTransform")
//    public Function<AllowedActions, ActionSet> availableActions() {
//        return (allowed) -> {
//            List<Action> list = allowed.getAvailableActions().stream()
//                .map(allowable -> { return new Action(allowable.getSystemName(), 
//                                                      allowable.getTitle(), 
//                                                      allowable.getDescription()); })
//                .collect(Collectors.toList());
//            ActionSet actions = new ActionSet();
//            actions.setActions(list);
//            return actions;
//        };
//    }
    
    @Bean(name="actionsModelTransform")
    public ActionsModelTransform modelTransform() {
        return new ActionsModelTransform();
    }
}
