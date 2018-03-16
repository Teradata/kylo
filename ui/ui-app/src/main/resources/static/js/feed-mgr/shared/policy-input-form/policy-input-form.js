define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    /**
     * the rule and options need to have been initialized by the PolicyInputFormService grouping
     *
     */
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                rule: '=',
                theForm: '=',
                feed: '=?',
                mode: '=',
                onPropertyChange: "&?"
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/policy-input-form/policy-input-form.html',
            controller: "PolicyInputFormController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var PolicyInputFormController = /** @class */ (function () {
        function PolicyInputFormController($scope, $q, PolicyInputFormService) {
            this.$scope = $scope;
            this.$q = $q;
            this.PolicyInputFormService = PolicyInputFormService;
            var self = this;
            self.editChips = {};
            self.editChips.selectedItem = null;
            self.editChips.searchText = null;
            self.validateRequiredChips = function (property) {
                return PolicyInputFormService.validateRequiredChips(self.theForm, property);
            };
            self.queryChipSearch = PolicyInputFormService.queryChipSearch;
            self.transformChip = PolicyInputFormService.transformChip;
            self.onPropertyChanged = function (property) {
                if (self.onPropertyChange != undefined && angular.isFunction(self.onPropertyChange)) {
                    self.onPropertyChange()(property);
                }
            };
            //call the onChange if the form initially sets the value
            if (self.onPropertyChange != undefined && angular.isFunction(self.onPropertyChange)) {
                _.each(self.rule.properties, function (property) {
                    if ((property.type == 'select' || property.type == 'feedSelect' || property.type == 'currentFeed') && property.value != null) {
                        self.onPropertyChange()(property);
                    }
                });
            }
        }
        ;
        return PolicyInputFormController;
    }());
    exports.PolicyInputFormController = PolicyInputFormController;
    angular.module(moduleName).controller('PolicyInputFormController', ["$scope", "$q", "PolicyInputFormService", PolicyInputFormController]);
    angular.module(moduleName)
        .directive('thinkbigPolicyInputForm', directive);
});
//# sourceMappingURL=policy-input-form.js.map