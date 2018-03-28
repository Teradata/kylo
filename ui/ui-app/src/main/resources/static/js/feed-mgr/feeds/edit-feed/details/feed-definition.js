define(["require", "exports", "angular", "pascalprecht.translate"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directiveConfig = function () {
        return {
            restrict: "EA",
            bindToController: {},
            scope: {
                versions: '=?'
            },
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-definition.html',
            controller: "FeedDefinitionController",
            link: function ($scope, element, attrs, controller) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
            }
        };
    };
    var FeedDefinitionController = /** @class */ (function () {
        function FeedDefinitionController($scope, $q, AccessControlService, EntityAccessControlService, FeedService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.FeedService = FeedService;
            this.$filter = $filter;
            this.versions = this.$scope.versions;
            /**
              * Indicates if the feed definitions may be edited. Editing is disabled if displaying Feed Versions
              * @type {boolean}
              */
            this.allowEdit = !this.versions;
            this.model = this.FeedService.editFeedModel;
            this.versionFeedModel = this.FeedService.versionFeedModel;
            this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
            this.editableSection = false;
            this.editModel = {};
            $scope.$watch(function () {
                return FeedService.editFeedModel;
            }, function (newVal) {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = angular.copy(FeedService.editFeedModel);
                }
            });
            if (this.versions) {
                $scope.$watch(function () {
                    return _this.FeedService.versionFeedModel;
                }, function (newVal) {
                    _this.versionFeedModel = _this.FeedService.versionFeedModel;
                });
                $scope.$watch(function () {
                    return _this.FeedService.versionFeedModelDiff;
                }, function (newVal) {
                    _this.versionFeedModelDiff = _this.FeedService.versionFeedModelDiff;
                });
            }
            //Apply the entity access permissions
            $q.when(AccessControlService.hasPermission(EntityAccessControlService.FEEDS_EDIT, this.model, EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                _this.allowEdit = !_this.versions && access && !_this.model.view.generalInfo.disabled;
            });
        }
        ;
        FeedDefinitionController.prototype.onEdit = function () {
            //copy the model
            var copy = this.FeedService.editFeedModel;
            this.editModel = {};
            this.editModel.feedName = copy.feedName;
            this.editModel.systemFeedName = copy.systemFeedName;
            this.editModel.description = copy.description;
            this.editModel.templateId = copy.templateId;
            this.editModel.allowIndexing = copy.allowIndexing;
        };
        FeedDefinitionController.prototype.onCancel = function () {
        };
        FeedDefinitionController.prototype.onSave = function (ev) {
            var _this = this;
            //save changes to the model
            this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-definition.Saving'), this.model.feedName);
            var copy = angular.copy(this.FeedService.editFeedModel);
            copy.feedName = this.editModel.feedName;
            copy.systemFeedName = this.editModel.systemFeedName;
            copy.description = this.editModel.description;
            copy.templateId = this.editModel.templateId;
            copy.userProperties = null;
            copy.allowIndexing = this.editModel.allowIndexing;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;
            this.FeedService.saveFeedModel(copy).then(function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.editableSection = false;
                //save the changes back to the model
                _this.model.feedName = _this.editModel.feedName;
                _this.model.systemFeedName = _this.editModel.systemFeedName;
                _this.model.description = _this.editModel.description;
                _this.model.templateId = _this.editModel.templateId;
                _this.model.allowIndexing = _this.editModel.allowIndexing;
                //Get the updated value from the server.
                _this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
            }, function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.FeedService.buildErrorData(_this.model.feedName, response);
                _this.FeedService.showFeedErrorsDialog();
                //make it editable
                _this.editableSection = true;
            });
        };
        ;
        FeedDefinitionController.prototype.diff = function (path) {
            return this.FeedService.diffOperation(path);
        };
        return FeedDefinitionController;
    }());
    exports.FeedDefinitionController = FeedDefinitionController;
    ;
    angular.module(moduleName).controller('FeedDefinitionController', ["$scope", "$q", "AccessControlService", "EntityAccessControlService", "FeedService", "$filter", FeedDefinitionController]);
    angular.module(moduleName)
        .directive('thinkbigFeedDefinition', directiveConfig);
});
//# sourceMappingURL=feed-definition.js.map