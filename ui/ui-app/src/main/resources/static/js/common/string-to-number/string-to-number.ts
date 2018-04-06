import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("stringToNumber",
  [ () => {
          return {
             require: 'ngModel',
            link: function(scope: any, element: any, attrs: any, ngModel: any) {
                ngModel.$parsers.push(function(value: any) {
                    return '' + value;
                });
                ngModel.$formatters.push(function(value: any) {
                    return parseFloat(value);
                    //parseFloat(value, 10);
                    /*Since parseFloat only parses numeric expressions in radix 10, there's no need for a radix parameter here.*/
                });
            }
        }
  }
  ]);