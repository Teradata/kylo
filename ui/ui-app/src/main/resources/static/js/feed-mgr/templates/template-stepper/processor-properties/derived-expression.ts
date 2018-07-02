import * as angular from 'angular';

import * as _ from "underscore";

import { moduleName } from "../../module-name";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';



var directive = function (RegisterTemplateService: RegisterTemplateServiceFactory) {

    return {
        restrict: "A",
        scope: {
            ngModel: '='
        },
        link: function ($scope: any, $element: any, attrs: any, controller: any) {
            $scope.$watch(
                'ngModel',
                function (newValue: any) {
                    if (newValue != null) {
                        var derivedValue = RegisterTemplateService.deriveExpression(newValue, true)
                        $element.html(derivedValue);
                    }
                    else {
                        $element.html('');
                    }
                });
        }
    };
}
angular.module(moduleName)

    .directive('thinkbigDerivedExpression', ['RegisterTemplateService', directive]);

