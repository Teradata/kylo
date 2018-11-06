import { Injectable } from "@angular/core";
import * as _ from "underscore";
import { downgradeInjectable } from "@angular/upgrade/static";
import * as angular from "angular";
import {moduleName} from "../../../module-name";

@Injectable()
export class FeedFieldPolicyRuleService {
    constructor() {

    }
    getAllPolicyRules = (field: any) => {
        if (field === undefined) {
            return [];
        }
        var arr = [];

        var standardizers = field['standardization'];
        var validators = field['validation'];

        //add in the type so we know what we are dealing with
        if (standardizers) {
            _.each(standardizers, (item: any) => {
                item.type = 'standardization';
            });
        }

        if (validators) {
            _.each(validators, (item: any) => {
                item.type = 'validation';
            });
        }

        var tmpArr = _.union(standardizers, validators);

        var hasSequence = _.find(tmpArr, (item: any) => {
            return item.sequence != null && item.sequence != undefined;
        }) !== undefined;

        //if we dont have a sequence, add it in
        if (!hasSequence) {
            _.each(tmpArr, (item: any, idx: any) => {
                item.sequence = idx;
            });
        }

        arr = _.sortBy(tmpArr, 'sequence');
        return arr;
    }
}
angular.module(moduleName).service('FeedFieldPolicyRuleService',downgradeInjectable(FeedFieldPolicyRuleService));