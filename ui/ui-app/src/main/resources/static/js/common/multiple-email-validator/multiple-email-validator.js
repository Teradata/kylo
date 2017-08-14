define(['angular','common/module-name'], function (angular,moduleName) {
    var directive = function () {
        //email regex courtesy of https://github.com/angular/angular.js/blob/master/src/ng/directive/input.js
        var EMAIL_REGEXP = /^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$/;

        var validatorName = 'multipleEmails';

        function validateEmails(ctrl, validatorName, emails) {
            var validity = ctrl.$isEmpty(emails) || emails.split(',').every(
                function (email) {
                    return EMAIL_REGEXP.test(email.trim());
                }
            );

            ctrl.$setValidity(validatorName, validity);
            return validity ? emails : undefined;
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function postLink(scope, elem, attrs, modelCtrl) {

                function multipleEmailValidator(value) {
                    return validateEmails(modelCtrl, validatorName, value);
                }

                modelCtrl.$formatters.push(multipleEmailValidator);
                modelCtrl.$parsers.push(multipleEmailValidator);
            }
        };
    };

    return angular.module(moduleName)
        .directive('multipleEmailValidator', [directive]);
});
