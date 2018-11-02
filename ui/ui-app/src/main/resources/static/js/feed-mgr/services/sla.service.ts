import * as _ from "underscore";
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {RestUrlConstants} from "./RestUrlConstants";

@Injectable()
export class SlaService {
    
    
    constructor(private http:HttpClient){
        
    }

            getPossibleSlaMetricOptions() {

                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }

            validateSlaActionClass(actionClass:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": actionClass}}).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            validateSlaActionRule(rule:any) {
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

                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": rule.objectClassType}}).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }

            getPossibleSlaActionOptions() {

                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }

            saveFeedSla(feedId:string, serviceLevelAgreement:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.post(RestUrlConstants.SAVE_FEED_SLA_URL(feedId),serviceLevelAgreement,{
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            saveSla(serviceLevelAgreement:any) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.post(RestUrlConstants.SAVE_SLA_URL,serviceLevelAgreement,{
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            deleteSla(slaId:string) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.delete(RestUrlConstants.DELETE_SLA_URL(slaId),{
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).toPromise();
                    promise.then(successFn, errorFn);
                return promise;

            }
            getSlaById(slaId:string) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_SLA_BY_ID_URL(slaId)).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            getSlaForEditForm(slaId:string) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_SLA_AS_EDIT_FORM(slaId)).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            getFeedSlas(feedId:string) {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_FEED_SLA_URL(feedId)).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }
            getAllSlas() {
                var successFn = function (response:any) {
                    return response.data;
                }
                var errorFn = function (err:any) {
                    console.log('ERROR ', err)
                }
                var promise = this.http.get(RestUrlConstants.GET_SLAS_URL).toPromise();
                promise.then(successFn, errorFn);
                return promise;

            }

    }

