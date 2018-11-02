import * as _ from "underscore"
import {DeployEntityVersionResponse} from "../model/deploy-entity-response.model";
import {FormGroup} from "@angular/forms";
import {NifiErrorMessage} from "../model/nifi-error-message.model";

export class FeedNifiErrorUtil {

    /**
     * parse the errors from the nifiFeed object and populate the errorMap
     * @param nifiFeed
     * @param errorMap
     * @return {number} the number of errors
     */
   public static  parseNifiFeedForErrors(nifiFeed:any, errorMap:{[key:string]: NifiErrorMessage[]}) {
        var count = 0;

        if (nifiFeed != null) {

            if (nifiFeed.errorMessages != null && nifiFeed.errorMessages.length > 0) {
                _.each(nifiFeed.errorMessages,  (msg:any) => {
                    errorMap['FATAL'].push({category: 'General', message: msg, severity: 'FATAL'});
                    count++;
                })
            }

            if (nifiFeed.feedProcessGroup != null) {
                _.each(nifiFeed.feedProcessGroup.errors,  (processor:any) =>{
                    if (processor.validationErrors) {
                        _.each(processor.validationErrors, (error:any) =>{
                            var copy:any = {};
                            _.extend(copy, error);
                            _.extend(copy, processor);
                            copy.validationErrors = null;
                            errorMap[error.severity].push(copy);
                            count++;
                        });
                    }
                });
            }
            if (errorMap['FATAL'].length == 0) {
                delete errorMap['FATAL'];
            }
            if (errorMap['WARN'].length == 0) {
                delete errorMap['WARN'];
            }
        }
        return count;

    }


    static parseDeployNiFiFeedErrors(deployResponse:DeployEntityVersionResponse) {
        var count = 0;
        var errorMap:any = {"FATAL": [], "WARN": []};
        deployResponse.errors = {message:"", errorMap:errorMap,errorCount:0}
        if (deployResponse.feed != null ) {

            count = FeedNifiErrorUtil.parseNifiFeedForErrors(deployResponse.feed, errorMap);
            deployResponse.errors.errorCount = count;
            deployResponse.errors.message = count +" NiFi errors exist";
        }
        if(count ==0) {
            if (deployResponse.httpStatus === 502) {
                deployResponse.errors.message = 'Error creating feed, bad gateway'
            } else if (deployResponse.httpStatus === 503) {
                deployResponse.errors.message = 'Error creating feed, service unavailable'
            } else if (deployResponse.httpStatus === 504) {
                deployResponse.errors.message = 'Error creating feed, gateway timeout'
            } else if (deployResponse.httpStatus === 504) {
                deployResponse.errors.message = 'Error creating feed, HTTP version not supported'
            } else {
                deployResponse.errors.message = 'Error creating feed.'
            }
        }

    }

}