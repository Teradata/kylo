import * as _ from "underscore";
import { Injectable, Inject } from '@angular/core';
import { RestUrlConstants } from './RestUrlConstants';

@Injectable()
export class SlaService {
    constructor (@Inject("$injector") private $injector: any) {}

    getPossibleSlaMetricOptions () {

        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL);
        promise.then(successFn, errorFn);
        return promise;

    }

    validateSlaActionClass (actionClass:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": actionClass}});
        promise.then(successFn, errorFn);
        return promise;

    }
    validateSlaActionRule (rule:any) {
        rule.validConfiguration = true;
        rule.validationMessage = '';
        var successFn = (response:any) => {
            if (response.data && response.data.length) {
                var validationMessage = "";
                _.each(response.data, (validation:any) => {
                    if (!validation.valid) {
                        if (validationMessage != "") {
                            validationMessage += ", ";
                        }
                        validationMessage += validation.validationMessage;
                    }
                });
                if (validationMessage != "") {
                    rule.validConfiguration = false;
                    rule.validationMessage = validationMessage;
                }
            }
            ;

        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": rule.objectClassType}});
        promise.then(successFn, errorFn);
        return promise;
    }

    getPossibleSlaActionOptions () {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL);
        promise.then(successFn, errorFn);
        return promise;
    }

    saveFeedSla (feedId:any, serviceLevelAgreement:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: RestUrlConstants.SAVE_FEED_SLA_URL(feedId),
            method: "POST",
            data: JSON.stringify(serviceLevelAgreement),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    saveSla (serviceLevelAgreement:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: RestUrlConstants.SAVE_SLA_URL,
            method: "POST",
            data: JSON.stringify(serviceLevelAgreement),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    deleteSla (slaId:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: RestUrlConstants.DELETE_SLA_URL(slaId),
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    getSlaById (slaId:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_SLA_BY_ID_URL(slaId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getSlaForEditForm (slaId:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_SLA_AS_EDIT_FORM(slaId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getFeedSlas (feedId:any) {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_FEED_SLA_URL(feedId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getAllSlas () {
        var successFn = (response:any) => {
            return response.data;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(RestUrlConstants.GET_SLAS_URL);
        promise.then(successFn, errorFn);
        return promise;

    }
}
