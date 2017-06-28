define(["angular"], function (angular) {

    /**
     * Directive for viewing and editing data transformation properties.
     */
    var kyloDataTransformProperties = function () {
        return {
            controller: "DataTransformPropertiesController",
            controllerAs: "vm",
            restrict: "E",
            templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
        };
    };

    /**
     * Controller for viewing and editing data transformation properties.
     * @constructor
     */
    var DataTransformPropertiesController = function ($scope, $q, AccessControlService, FeedService, StateService, VisualQueryService) {
        var self = this;

        /**
         * Indicates if the feed NiFi properties may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Indicates that the editable section is visible.
         * @type {boolean}
         */
        self.editableSection = false;

        /**
         * Feed model.
         * @type {Object}
         */
        self.model = FeedService.editFeedModel;

        // Watch for model changes
        $scope.$watch(function () {
            return FeedService.editFeedModel;
        }, function () {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = angular.copy(FeedService.editFeedModel);
            }
        });

        /**
         * Navigates to the Edit Feed page for the current feed.
         */
        this.navigateToEditFeedInStepper = function () {
            VisualQueryService.resetModel();
            StateService.FeedManager().Feed().navigateToEditFeedInStepper(self.model.feedId);
        };

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT, self.model, AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
            self.allowEdit = access;
        });
    };

    angular.module("kylo.plugin.template-table-option.data-transformation", [])
        .controller('DataTransformPropertiesController', ["$scope", "$q", "AccessControlService", "FeedService", "StateService", "VisualQueryService", DataTransformPropertiesController])
        .directive('kyloDataTransformProperties', kyloDataTransformProperties);
});
