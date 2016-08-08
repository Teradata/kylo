package com.thinkbiganalytics.metadata.sla.alerts;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.apache.commons.lang3.reflect.ConstructorUtils;
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
 * Created by sr186054 on 8/7/16.
 */
public class ServiceLevelAgreementActionUtil {


    Timer invalidActionConfigurationCacheTimer = new Timer();

    public static Map<Class<? extends ServiceLevelAgreementAction>, Boolean> validActionCache = new ConcurrentHashMap<>();

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

    public ServiceLevelAgreementActionUtil() {
        scheduleCacheCheck();
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

    private void scheduleCacheCheck() {
        invalidActionConfigurationCacheTimer.schedule(new EvaluateInvalidActionsTimer(), (60 * 1000 * 5), (60 * 1000) * 60); // delay 5 min, 1 hr eval
    }


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
                //TODO LOG error
                e.printStackTrace();
            }
        }
        return action;
    }

}
