import * as angular from 'angular';
import * as _ from "underscore";
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from './RestUrlService';

@Injectable()
export class SlaService {
    constructor (private RestUrlService: RestUrlService,
                @Inject("$injector") private $injector: any) {}

    getPossibleSlaMetricOptions= () => {

        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL);
        promise.then(successFn, errorFn);
        return promise;

    }

    validateSlaActionClass= (actionClass:any) => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": actionClass}});
        promise.then(successFn, errorFn);
        return promise;

    }
    validateSlaActionRule= (rule:any) => {
        rule.validConfiguration = true;
        rule.validationMessage = '';
        var successFn = function (response:any) {
            if (response.data && response.data.length) {
                var validationMessage = "";
                _.each(response.data, function (validation:any) {
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
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": rule.objectClassType}});
        promise.then(successFn, errorFn);
        return promise;

    }

    getPossibleSlaActionOptions= () => {

        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL);
        promise.then(successFn, errorFn);
        return promise;

    }

    saveFeedSla= (feedId:any, serviceLevelAgreement:any)=>  {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: this.RestUrlService.SAVE_FEED_SLA_URL(feedId),
            method: "POST",
            data: angular.toJson(serviceLevelAgreement),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    saveSla= (serviceLevelAgreement:any) => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: this.RestUrlService.SAVE_SLA_URL,
            method: "POST",
            data: angular.toJson(serviceLevelAgreement),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    deleteSla= (slaId:any) => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http")({
            url: this.RestUrlService.DELETE_SLA_URL(slaId),
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        }).then(successFn, errorFn);
        return promise;

    }
    getSlaById= (slaId:any) =>  {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_SLA_BY_ID_URL(slaId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getSlaForEditForm= (slaId:any) => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_SLA_AS_EDIT_FORM(slaId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getFeedSlas= (feedId:any) => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_FEED_SLA_URL(feedId));
        promise.then(successFn, errorFn);
        return promise;

    }
    getAllSlas= () => {
        var successFn = function (response:any) {
            return response.data;
        }
        var errorFn = function (err:any) {
            console.log('ERROR ', err)
        }
        var promise = this.$injector.get("$http").get(this.RestUrlService.GET_SLAS_URL);
        promise.then(successFn, errorFn);
        return promise;

    }
    
}
