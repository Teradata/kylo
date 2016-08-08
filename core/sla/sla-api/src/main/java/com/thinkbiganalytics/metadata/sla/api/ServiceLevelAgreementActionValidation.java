package com.thinkbiganalytics.metadata.sla.api;

/**
 * Created by sr186054 on 8/8/16.
 */
public class ServiceLevelAgreementActionValidation {

    private String actionClass;

    private boolean valid;

    public String validationMessage;

    public static ServiceLevelAgreementActionValidation VALID = new ServiceLevelAgreementActionValidation(true);

    public static ServiceLevelAgreementActionValidation INVALID = new ServiceLevelAgreementActionValidation(false);

    public ServiceLevelAgreementActionValidation(boolean valid) {
        this.valid = valid;
    }

    public static ServiceLevelAgreementActionValidation valid(Class actionClass) {
        ServiceLevelAgreementActionValidation validation = new ServiceLevelAgreementActionValidation(true);
        validation.setActionClass(actionClass.getName());
        return validation;
    }

    public ServiceLevelAgreementActionValidation(boolean valid, String validationMessage) {
        this.valid = valid;
        this.validationMessage = validationMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }
}
