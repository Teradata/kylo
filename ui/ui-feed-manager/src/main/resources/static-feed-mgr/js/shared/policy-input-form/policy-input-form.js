(function () {

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
            templateUrl: 'js/shared/policy-input-form/policy-input-form.html',
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

    angular.module(MODULE_FEED_MGR).controller('PolicyInputFormController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigPolicyInputForm', directive);

})();
