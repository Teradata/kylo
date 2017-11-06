package com.thinkbiganalytics.metadata.sla.alerts;

/*-
 * #%L
 * thinkbig-sla-core
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class ServiceLevelAgreementActionUtil {
    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementActionUtil.class);

    protected static final Map<Class<? extends ServiceLevelAgreementAction>, Boolean> validActionCache = new ConcurrentHashMap<>();
    Timer invalidActionConfigurationCacheTimer = new Timer();

    public ServiceLevelAgreementActionUtil() {
        scheduleCacheCheck();
    }

    public static boolean isValidConfiguration(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {

        List<ServiceLevelAgreementActionValidation> validation = validateActionConfiguration(actionClasses);
        return Iterables.any(validation, new Predicate<ServiceLevelAgreementActionValidation>() {
            @Override
            public boolean apply(ServiceLevelAgreementActionValidation serviceLevelAgreementActionValidation) {
                return !serviceLevelAgreementActionValidation.isValid();
            }
        });

    }

    public static ServiceLevelAgreementActionValidation validateConfiguration(ServiceLevelAgreementAction action) {

        if (validActionCache.containsKey(action.getClass()) && validActionCache.get(action.getClass()) == true) {
            return ServiceLevelAgreementActionValidation.VALID;
        }
        if (action != null) {
            ServiceLevelAgreementActionValidation validAction = action.validateConfiguration();
            validAction.setActionClass(action.getClass().getName());
            validActionCache.put(action.getClass(), validAction.isValid());
            return validAction;
        }
        return new ServiceLevelAgreementActionValidation(false, "Unable to find SLA Action ");

    }

    public static List<ServiceLevelAgreementActionValidation> validateActionConfiguration(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {

        List<ServiceLevelAgreementActionValidation> validation = new ArrayList<>();

        if (actionClasses != null) {
            for (Class<? extends ServiceLevelAgreementAction> actionClass : actionClasses) {
                if (validActionCache.containsKey(actionClass) && validActionCache.get(actionClass) == true) {
                    validation.add(ServiceLevelAgreementActionValidation.valid(actionClass));
                }
                ServiceLevelAgreementAction action = ServiceLevelAgreementActionUtil.instantiate(actionClass);
                if (action != null) {
                    validation.add(validateConfiguration(action));
                }
            }

        }
        return validation;

    }
    //re-evaluate invalid actions ever 1 hr

    public static ServiceLevelAgreementAction instantiate(Class<? extends ServiceLevelAgreementAction> clazz) {
        ServiceLevelAgreementAction action = null;
        try {
            action = SpringApplicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            //this is ok
        }

        //if not spring bound then construct the Responder
        if (action == null) {
            //construct and invoke
            try {
                action = ConstructorUtils.invokeConstructor(clazz, null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                log.error("Error invoking constructor", e);
            }
        }
        return action;
    }

    private void scheduleCacheCheck() {
        invalidActionConfigurationCacheTimer.schedule(new EvaluateInvalidActionsTimer(), (60 * 1000 * 5), (60 * 1000) * 60); // delay 5 min, 1 hr eval
    }

    private class EvaluateInvalidActionsTimer extends TimerTask {

        @Override
        public void run() {

            Iterator<Map.Entry<Class<? extends ServiceLevelAgreementAction>, Boolean>> iter = validActionCache.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Class<? extends ServiceLevelAgreementAction>, Boolean> entry = iter.next();
                if (entry.getValue() != null && entry.getValue().booleanValue() == false) {
                    iter.remove();
                }
            }

        }
    }

}
