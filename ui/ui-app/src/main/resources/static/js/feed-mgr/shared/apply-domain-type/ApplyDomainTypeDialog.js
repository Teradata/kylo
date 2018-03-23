define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var ApplyDomainTypeDialogController = /** @class */ (function () {
        /**
         * Controller for the dialog that confirms overwriting a field with domain type policies.
         * @constructor
         */
        function ApplyDomainTypeDialogController($scope, $mdDialog, domainType, field) {
            /**
             * Function for canceling the dialog.
             * @type {Function}
             */
            $scope.cancel = $mdDialog.cancel;
            /**
             * The domain type to apply.
             * @type {DomainType}
             */
            $scope.domainType = domainType;
            /**
             * The field to be overwritten.
             * @type {Field}
             */
            $scope.field = field;
            /**
             * Function for accepting the dialog.
             * @type {Function}
             */
            $scope.hide = $mdDialog.hide;
            /**
             * List of standardizer names.
             * @type {string}
             */
            $scope.standardizerList = domainType.fieldPolicy.standardization.map(_.property("name")).join(", ");
            /**
             * List of tag names.
             * @type {string}
             */
            $scope.tagList = angular.isArray(domainType.field.tags) ? domainType.field.tags.map(_.property("name")).join(", ") : "";
            /**
             * List of validator names.
             * @type {string}
             */
            $scope.validatorList = domainType.fieldPolicy.validation.map(_.property("name")).join(", ");
        }
        ;
        return ApplyDomainTypeDialogController;
    }());
    exports.ApplyDomainTypeDialogController = ApplyDomainTypeDialogController;
    angular.module(moduleName).controller("ApplyDomainTypeDialogController", ["$scope", "$mdDialog", "domainType", "field", ApplyDomainTypeDialogController]);
});
//# sourceMappingURL=ApplyDomainTypeDialog.js.map