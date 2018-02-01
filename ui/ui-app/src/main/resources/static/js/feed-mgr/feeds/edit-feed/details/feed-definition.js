define(["require", "exports", "angular", "pascalprecht.translate"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directiveConfig = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-definition.html',
            controller: "FeedDefinitionController",
            link: function ($scope, element, attrs, controller) {
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
            /**
              * Indicates if the feed definitions may be edited.
              * @type {boolean}
              */
            this.allowEdit = false;
            this.model = this.FeedService.editFeedModel;
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
            //Apply the entity access permissions
            $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT, this.model, AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                _this.allowEdit = access && !_this.model.view.generalInfo.disabled;
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
            this.FeedService.saveFeedModel(copy).then(function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.editableSection = false;
                //save the changes back to the model
                _this.model.feedName = _this.editModel.feedName;
                _this.model.systemFeedName = _this.editModel.systemFeedName;
                _this.model.description = _this.editModel.description;
                _this.model.templateId = _this.editModel.templateId;
            }, function (response) {
                _this.FeedService.hideFeedSavingDialog();
                _this.FeedService.buildErrorData(_this.model.feedName, response);
                _this.FeedService.showFeedErrorsDialog();
                //make it editable
                _this.editableSection = true;
            });
        };
        ;
        return FeedDefinitionController;
    }());
    exports.FeedDefinitionController = FeedDefinitionController;
    ;
    angular.module(moduleName).controller('FeedDefinitionController', ["$scope", "$q", "AccessControlService", "EntityAccessControlService", "FeedService", "$filter", FeedDefinitionController]);
    angular.module(moduleName)
        .directive('thinkbigFeedDefinition', directiveConfig);
});
//# sourceMappingURL=feed-definition.js.map