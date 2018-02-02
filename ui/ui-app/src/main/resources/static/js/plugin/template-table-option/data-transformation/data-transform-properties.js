var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
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
            this.$scope.$watch(function () { return FeedService.editFeedModel; }, function () {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = angular.copy(FeedService.editFeedModel);
                }
            });
            /**
             * Navigates to the Edit Feed page for the current feed.
             */
            navigateToEditFeedInStepper = function () { return function () {
                _this.VisualQueryService.resetModel();
                _this.StateService.FeedManager().Feed().navigateToEditFeedInStepper(_this.model.feedId);
            }; };
            //Apply the entity access permissions
            this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS))
                .then(function (access) {
                _this.allowEdit = access;
            });
        }
        return DataTransformPropertiesController;
    }());
    exports.DataTransformPropertiesController = DataTransformPropertiesController;
    var kyloDataTransformProperties = /** @class */ (function (_super) {
        __extends(kyloDataTransformProperties, _super);
        function kyloDataTransformProperties() {
            var _this = this;
            return {
                controller: "DataTransformPropertiesController",
                controllerAs: "vm",
                restrict: "E",
                templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
            };
        }
        return kyloDataTransformProperties;
    }(ng.IDirective));
    exports.kyloDataTransformProperties = kyloDataTransformProperties;
    angular.module("kylo.plugin.template-table-option.data-transformation", [])
        .controller('DataTransformPropertiesController', ["$scope",
        "$q",
        "AccessControlService",
        "FeedService",
        "StateService",
        "VisualQueryService",
        DataTransformPropertiesController])
        .directive('kyloDataTransformProperties', kyloDataTransformProperties);
});
//# sourceMappingURL=data-transform-properties.js.map