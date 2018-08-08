import * as _ from "underscore";
import { Injectable, Inject } from '@angular/core';
import { RestUrlConstants } from './RestUrlConstants';
import { HttpClient,  HttpHeaders } from "@angular/common/http";

@Injectable()
export class SlaService {
    constructor (private http : HttpClient) {}

    getPossibleSlaMetricOptions () {

        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_POSSIBLE_SLA_METRIC_OPTIONS_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }

    validateSlaActionClass (actionClass:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": actionClass}}).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }
    validateSlaActionRule (rule:any) {
        rule.validConfiguration = true;
        rule.validationMessage = '';
        var successFn = (response:any) => {
            if (response && response.length) {
                var validationMessage = "";
                _.each(response, (validation:any) => {
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
        var promise = this.http.get(RestUrlConstants.VALIDATE_SLA_ACTION_URL, {params: {"actionConfigClass": rule.objectClassType}}).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    getPossibleSlaActionOptions () {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_POSSIBLE_SLA_ACTION_OPTIONS_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    saveFeedSla (feedId:any, serviceLevelAgreement:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.post(RestUrlConstants.SAVE_FEED_SLA_URL(feedId),JSON.stringify(serviceLevelAgreement),
                        {headers :new HttpHeaders({'Content-Type':'application/json; charset=utf-8'}) }).toPromise().then(successFn, errorFn);
        return promise;

    }
    saveSla (serviceLevelAgreement:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.post(RestUrlConstants.SAVE_SLA_URL,JSON.stringify(serviceLevelAgreement),
                        {headers :new HttpHeaders({'Content-Type':'application/json; charset=utf-8'}) }).toPromise().then(successFn, errorFn);
        return promise;

    }
    deleteSla (slaId:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.delete(RestUrlConstants.DELETE_SLA_URL(slaId)).toPromise().then(successFn, errorFn);
        return promise;

    }
    getSlaById (slaId:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_SLA_BY_ID_URL(slaId)).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }
    getSlaForEditForm (slaId:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_SLA_AS_EDIT_FORM(slaId)).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }
    getFeedSlas (feedId:any) {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_FEED_SLA_URL(feedId)).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }
    getAllSlas () {
        var successFn = (response:any) => {
            return response;
        }
        var errorFn = (err:any) => {
            console.log('ERROR ', err)
        }
        var promise = this.http.get(RestUrlConstants.GET_SLAS_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;

    }
}
