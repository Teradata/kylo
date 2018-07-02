import * as angular from "angular";
import {moduleName} from "../module-name";




angular.module(moduleName)
        .directive('multipleEmailValidator', [
            ()=>
            {
        //email regex courtesy of https://github.com/angular/angular.js/blob/master/src/ng/directive/input.js
        var EMAIL_REGEXP = /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;

        var validatorName = 'multipleEmails';

        function validateEmails(ctrl: any, validatorName: any, emails: any) {
            var validity = ctrl.$isEmpty(emails) || emails.split(',').every(
                function (email: any) {
                    return EMAIL_REGEXP.test(email.trim());
                }
            );

            ctrl.$setValidity(validatorName, validity);
            return validity ? emails : undefined;
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function postLink(scope: any, elem: any, attrs: any, modelCtrl: any) {

                function multipleEmailValidator(value: any) {
                    return validateEmails(modelCtrl, validatorName, value);
                }

                modelCtrl.$formatters.push(multipleEmailValidator);
                modelCtrl.$parsers.push(multipleEmailValidator);
            }
        };
    }
        ]);
