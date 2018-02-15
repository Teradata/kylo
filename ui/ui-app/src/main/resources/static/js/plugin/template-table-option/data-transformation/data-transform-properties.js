define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var DataTransformPropertiesController = /** @class */ (function () {
        function DataTransformPropertiesController($scope, $q, AccessControlService, FeedService, StateService, VisualQueryService) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.AccessControlService = AccessControlService;
            this.FeedService = FeedService;
            this.StateService = StateService;
            this.VisualQueryService = VisualQueryService;
            /**
             * Indicates if the feed NiFi properties may be edited.
             * @type {boolean}
             */
            this.allowEdit = false;
            /**
             * Indicates that the editable section is visible.
             * @type {boolean}
             */
            this.editableSection = false;
            /**
             * Feed model.
             * @type {Object}
             */
            this.model = this.FeedService.editFeedModel;
            // Watch for model changes
            this.$scope.$watch(function () {
                return FeedService.editFeedModel;
            }, function () {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = angular.copy(FeedService.editFeedModel);
                }
            });
            //Apply the entity access permissions
            this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS))
                .then(function (access) {
                _this.allowEdit = access;
            });
        }
        /**
         * Navigates to the Edit Feed page for the current feed.
         */
        DataTransformPropertiesController.prototype.navigateToEditFeedInStepper = function () {
            this.VisualQueryService.resetModel();
            this.StateService.FeedManager().Feed().navigateToEditFeedInStepper(this.model.feedId);
        };
        DataTransformPropertiesController.$inject = ["$scope", "$q", "AccessControlService", "FeedService", "StateService", "VisualQueryService"];
        return DataTransformPropertiesController;
    }());
    exports.DataTransformPropertiesController = DataTransformPropertiesController;
    angular.module("kylo.plugin.template-table-option.data-transformation", [])
        .component("kyloDataTransformProperties", {
        controller: DataTransformPropertiesController,
        controllerAs: "vm",
        templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
    });
});
//# sourceMappingURL=data-transform-properties.js.map