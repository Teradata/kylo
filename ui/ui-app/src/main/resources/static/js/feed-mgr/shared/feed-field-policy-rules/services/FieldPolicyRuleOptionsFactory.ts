import { HttpClient } from "@angular/common/http";
import { RestUrlService } from "../../../services/RestUrlService";
import { Inject, Injectable } from "@angular/core";
import { downgradeInjectable } from "@angular/upgrade/static";
import * as angular from "angular";
import {moduleName} from "../../../module-name";
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/forkJoin';

@Injectable()
export class FieldPolicyRuleOptionsFactory {

    standardizationOptions: any[] = [];
    validationOptions: any[] = [];

    constructor(private http: HttpClient, 
                private RestUrlService: RestUrlService) {

    }
    getStandardizationOptions() {
        return this.http.get(this.RestUrlService.AVAILABLE_STANDARDIZATION_POLICIES).toPromise();
    }

    getValidationOptions() {
        return this.http.get(this.RestUrlService.AVAILABLE_VALIDATION_POLICIES).toPromise();
    }

    getParserOptions() {
        return this.http.get(this.RestUrlService.LIST_FILE_PARSERS).toPromise();
    }

    getSparkParserOptions() {
        return this.http.get(this.RestUrlService.LIST_SPARK_FILE_PARSERS).toPromise();
    }
    getOptionsForType (type: any) {
        if (type == 'standardization-validation') {
            return Observable.forkJoin([this.getValidationOptions(), this.getStandardizationOptions()]);
        }
        if (type == 'standardization') {
            return this.getStandardizationOptions();
        }
        else if (type == 'validation') {
            return this.getValidationOptions();
        }
        else if (type == 'schemaParser') {
            return this.getParserOptions();
        }
        else if (type == 'sparkSchemaParser') {
            return this.getSparkParserOptions();
        }
    }
    getTitleForType = (type: any) => {
        if (type == 'standardization') {
            return "Standardization Policies";
        }
        else if (type == 'validation') {
            return 'Validation Policies';
        } else if (type == 'schemaParser') {
            return 'Supported Parsers'
        } else if (type == 'schemaParser') {
            return 'Supported Parsers'
        }

    }
    getStandardizersAndValidators (){
        return this.getOptionsForType('standardization-validation');
    }
}
angular.module(moduleName).service('FieldPolicyRuleOptionsFactory',downgradeInjectable(FieldPolicyRuleOptionsFactory));