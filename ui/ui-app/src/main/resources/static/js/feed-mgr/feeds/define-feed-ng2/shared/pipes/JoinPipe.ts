import {Pipe, PipeTransform} from '@angular/core';
import * as _ from "underscore";

@Pipe({name:'join'})
export class JoinPipe implements PipeTransform {
    transform(arr: any, joinChar: any, childProperty: any): string {

        if (joinChar === undefined) {
            joinChar = ',';
        }

        if (_.isArray(arr)) {
            if (!childProperty) {
                return arr.join(joinChar);
            }
            else {
                return _.map(arr, function (item: any) {
                    return item[childProperty];
                }).join(joinChar);
            }
        }
        return arr;
    }
}