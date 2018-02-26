var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "angular", "@angular/core"], function (require, exports, angular, core_1) {
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
            this.versionModel = this.FeedService.versionFeedModel;
            this.diff = function (path) {
                return this.FeedService.diffOperation(path);
            };
            this.diffCollection = function (path) {
                return this.FeedService.diffCollectionOperation(path);
            };
            // Watch for model changes
            this.$scope.$watch(function () {
                return FeedService.editFeedModel;
            }, function () {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = angular.copy(FeedService.editFeedModel);
                }
            });
            if (this.versions) {
                this.$scope.$watch(function () {
                    return FeedService.versionFeedModel;
                }, function () {
                    _this.versionModel = angular.copy(FeedService.versionFeedModel);
                });
            }
            //Apply the entity access permissions
            this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS))
                .then(function (access) {
                _this.allowEdit = !_this.versions && access;
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
        __decorate([
            core_1.Input(),
            __metadata("design:type", Boolean)
        ], DataTransformPropertiesController.prototype, "versions", void 0);
        return DataTransformPropertiesController;
    }());
    exports.DataTransformPropertiesController = DataTransformPropertiesController;
    angular.module("kylo.plugin.template-table-option.data-transformation", [])
        .component("kyloDataTransformProperties", {
        bindings: {
            versions: "<"
        },
        controller: DataTransformPropertiesController,
        controllerAs: "vm",
        templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
    });
});
//# sourceMappingURL=data-transform-properties.js.map