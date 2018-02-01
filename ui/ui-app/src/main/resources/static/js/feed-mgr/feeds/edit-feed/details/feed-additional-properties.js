define(["require", "exports", "angular", "pascalprecht.translate"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
            controller: "FeedAdditionalPropertiesController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var FeedAdditionalPropertiesController = /** @class */ (function () {
        function FeedAdditionalPropertiesController($scope, $q, AccessControlService, EntityAccessControlService, FeedService, FeedTagService, FeedSecurityGroups, $filter) {
            this.$scope = $scope;
            this.$q = $q;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.FeedService = FeedService;
            this.FeedTagService = FeedTagService;
            this.FeedSecurityGroups = FeedSecurityGroups;
            this.$filter = $filter;
            // define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {
            /**
             * Indicates if the feed properties may be edited.
             * @type {boolean}
             */
            this.allowEdit = false;
            this.model = this.FeedService.editFeedModel;
            this.editModel = {};
            this.editableSection = false;
            this.feedTagService = this.FeedTagService;
            this.tagChips = {};
            this.securityGroupChips = {};
            this.isValid = true;
            this.feedSecurityGroups = this.FeedSecurityGroups;
            this.securityGroupsEnabled = false;
            this.transformChip = function (chip) {
                // If it is an object, it's already a known chip
                if (angular.isObject(chip)) {
                    return chip;
                }
                // Otherwise, create a new one
                return { name: chip };
            };
            this.onEdit = function () {
                // Determine tags value
                var tags = angular.copy(this.FeedService.editFeedModel.tags);
                if (tags == undefined || tags == null) {
                    tags = [];
                }
                // Copy model for editing
                this.editModel = {};
                this.editModel.dataOwner = this.model.dataOwner;
                this.editModel.tags = tags;
                this.editModel.userProperties = angular.copy(this.model.userProperties);
                this.editModel.securityGroups = angular.copy(this.FeedService.editFeedModel.securityGroups);
                if (this.editModel.securityGroups == undefined) {
                    this.editModel.securityGroups = [];
                }
            };
            this.onCancel = function () {
                // do nothing
            };
            this.onSave = function (ev) {
                var _this = this;
                //save changes to the model
                this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-additional-properties.Saving'), this.model.feedName);
                var copy = angular.copy(this.FeedService.editFeedModel);
                copy.tags = this.editModel.tags;
                copy.dataOwner = this.editModel.dataOwner;
                copy.userProperties = this.editModel.userProperties;
                copy.securityGroups = this.editModel.securityGroups;
                this.FeedService.saveFeedModel(copy).then(function (response) {
                    _this.FeedService.hideFeedSavingDialog();
                    _this.editableSection = false;
                    //save the changes back to the model
                    _this.model.tags = _this.editModel.tags;
                    _this.model.dataOwner = _this.editModel.dataOwner;
                    _this.model.userProperties = _this.editModel.userProperties;
                    _this.model.securityGroups = _this.editModel.securityGroups;
                }, function (response) {
                    _this.FeedService.hideFeedSavingDialog();
                    _this.FeedService.buildErrorData(_this.model.feedName, response);
                    _this.FeedService.showFeedErrorsDialog();
                    //make it editable
                    _this.editableSection = true;
                });
            };
            var self = this;
            this.tagChips.selectedItem = null;
            this.tagChips.searchText = null;
            this.securityGroupChips.selectedItem = null;
            this.securityGroupChips.searchText = null;
            FeedSecurityGroups.isEnabled().then(function (isValid) {
                self.securityGroupsEnabled = isValid;
            });
            $scope.$watch(function () {
                return FeedService.editFeedModel;
            }, function (newVal) {
                //only update the model if it is not set yet
                if (self.model == null) {
                    self.model = FeedService.editFeedModel;
                }
            });
            //Apply the entity access permissions
            $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT, self.model, AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                self.allowEdit = access && !self.model.view.properties.disabled;
            });
        }
        return FeedAdditionalPropertiesController;
    }());
    exports.FeedAdditionalPropertiesController = FeedAdditionalPropertiesController;
    angular.module(moduleName).controller('FeedAdditionalPropertiesController', ["$scope", "$q", "AccessControlService", "EntityAccessControlService", "FeedService", "FeedTagService", "FeedSecurityGroups", "$filter", FeedAdditionalPropertiesController]);
    angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
});
//# sourceMappingURL=feed-additional-properties.js.map