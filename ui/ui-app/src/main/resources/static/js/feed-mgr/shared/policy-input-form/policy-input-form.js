define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {

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
                mode: '&' //NEW or EDIT
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/shared/policy-input-form/policy-input-form.html',
            controller: "PolicyInputFormController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $q, PolicyInputFormService) {

        var self = this;
        self.editChips = {};
        self.editChips.selectedItem = null;
        self.editChips.searchText = null;
        self.validateRequiredChips = function (property) {
            return PolicyInputFormService.validateRequiredChips(self.theForm, property);
        }
        self.queryChipSearch = PolicyInputFormService.queryChipSearch;
        self.transformChip = PolicyInputFormService.transformChip;

    };


    angular.module(moduleName).controller('PolicyInputFormController', ["$scope","$q","PolicyInputFormService",controller]);

    angular.module(moduleName)
        .directive('thinkbigPolicyInputForm', directive);

});
