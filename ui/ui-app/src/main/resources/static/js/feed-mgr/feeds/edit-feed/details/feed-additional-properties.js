define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
            controller: "FeedAdditionalPropertiesController",
            link: function ($scope, element, attrs, controller) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
            }
        };
    };
    var FeedAdditionalPropertiesController = /** @class */ (function () {
        function FeedAdditionalPropertiesController($scope, $q, accessControlService, EntityAccessControlService, FeedService, FeedTagService, FeedSecurityGroups, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.accessControlService = accessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.FeedService = FeedService;
            this.FeedTagService = FeedTagService;
            this.FeedSecurityGroups = FeedSecurityGroups;
            this.$filter = $filter;
            // define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {
            this.versions = this.$scope.versions;
            /**
             * Indicates if the feed properties may be edited.
             * @type {boolean}
             */
            this.allowEdit = !this.versions;
            this.model = this.FeedService.editFeedModel;
            this.versionFeedModel = this.FeedService.versionFeedModel;
            this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
            this.editModel = {};
            this.editableSection = false;
            this.feedTagService = this.FeedTagService;
            this.tagChips = {};
            this.securityGroupChips = {};
            this.isValid = true;
            this.feedSecurityGroups = this.FeedSecurityGroups;
            this.securityGroupsEnabled = false;
            this.userProperties = [];
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
                var tags = angular.copy(_this.FeedService.editFeedModel.tags);
                if (tags == undefined || tags == null) {
                    tags = [];
                }
                // Copy model for editing
                _this.editModel = {};
                _this.editModel.dataOwner = _this.model.dataOwner;
                _this.editModel.tags = tags;
                _this.editModel.userProperties = angular.copy(_this.model.userProperties);
                _this.editModel.securityGroups = angular.copy(_this.FeedService.editFeedModel.securityGroups);
                if (_this.editModel.securityGroups == undefined) {
                    _this.editModel.securityGroups = [];
                }
            };
            this.onCancel = function () {
                // do nothing
            };
            this.onSave = function (ev) {
                //save changes to the model
                _this.FeedService.showFeedSavingDialog(ev, _this.$filter('translate')('views.feed-additional-properties.Saving'), _this.model.feedName);
                var copy = angular.copy(_this.FeedService.editFeedModel);
                copy.tags = _this.editModel.tags;
                copy.dataOwner = _this.editModel.dataOwner;
                copy.userProperties = _this.editModel.userProperties;
                copy.securityGroups = _this.editModel.securityGroups;
                //Server may have updated value. Don't send via UI.
                copy.historyReindexingStatus = undefined;
                _this.FeedService.saveFeedModel(copy).then(function (response) {
                    _this.FeedService.hideFeedSavingDialog();
                    _this.editableSection = false;
                    //save the changes back to the model
                    _this.model.tags = _this.editModel.tags;
                    _this.model.dataOwner = _this.editModel.dataOwner;
                    _this.model.userProperties = _this.editModel.userProperties;
                    _this.model.securityGroups = _this.editModel.securityGroups;
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
            // var self = this;
            this.tagChips.selectedItem = null;
            this.tagChips.searchText = null;
            this.securityGroupChips.selectedItem = null;
            this.securityGroupChips.searchText = null;
            FeedSecurityGroups.isEnabled().then(function (isValid) {
                _this.securityGroupsEnabled = isValid;
            });
            $scope.$watch(function () {
                return FeedService.editFeedModel;
            }, function (newVal) {
                //only update the model if it is not set yet
                if (_this.model == null) {
                    _this.model = FeedService.editFeedModel;
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
                    _this.userProperties = [];
                    _.each(_this.versionFeedModel.userProperties, function (versionedProp) {
                        var property = {};
                        property.versioned = angular.copy(versionedProp);
                        property.op = 'no-op';
                        property.systemName = property.versioned.systemName;
                        property.displayName = property.versioned.displayName;
                        property.description = property.versioned.description;
                        property.current = angular.copy(property.versioned);
                        _this.userProperties.push(property);
                    });
                    _.each(_.values(_this.versionFeedModelDiff), function (diff) {
                        if (diff.path.startsWith("/userProperties")) {
                            if (diff.path.startsWith("/userProperties/")) {
                                //individual versioned indexed action
                                var remainder = diff.path.substring("/userProperties/".length, diff.path.length);
                                var indexOfSlash = remainder.indexOf("/");
                                var versionedPropIdx = remainder.substring(0, indexOfSlash > 0 ? indexOfSlash : remainder.length);
                                if ("replace" === diff.op) {
                                    var property = _this.userProperties[versionedPropIdx];
                                    property.op = diff.op;
                                    var replacedPropertyName = remainder.substring(remainder.indexOf("/") + 1, remainder.length);
                                    property.current[replacedPropertyName] = diff.value;
                                    property[replacedPropertyName] = diff.value;
                                }
                                else if ("add" === diff.op) {
                                    if (_.isArray(diff.value)) {
                                        _.each(diff.value, function (prop) {
                                            _this.userProperties.push(_this.createProperty(prop, diff.op));
                                        });
                                    }
                                    else {
                                        _this.userProperties.unshift(_this.createProperty(diff.value, diff.op));
                                    }
                                }
                                else if ("remove" === diff.op) {
                                    var property = _this.userProperties[versionedPropIdx];
                                    property.op = diff.op;
                                    property.current = {};
                                }
                            }
                            else {
                                //group versioned action, can be either "add" or "remove"
                                if ("add" === diff.op) {
                                    if (_.isArray(diff.value)) {
                                        _.each(diff.value, function (prop) {
                                            _this.userProperties.push(_this.createProperty(prop, diff.op));
                                        });
                                    }
                                    else {
                                        _this.userProperties.push(_this.createProperty(diff.value, diff.op));
                                    }
                                }
                                else if ("remove" === diff.op) {
                                    _.each(_this.userProperties, function (prop) {
                                        prop.op = diff.op;
                                        prop.current = {};
                                    });
                                }
                            }
                        }
                    });
                });
            }
            //Apply the entity access permissions
            $q.when(accessControlService.hasPermission(EntityAccessControlService.FEEDS_EDIT, this.model, EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function (access) {
                _this.allowEdit = !_this.versions && access && !_this.model.view.properties.disabled;
            });
        }
        FeedAdditionalPropertiesController.prototype.createProperty = function (original, operation) {
            var property = {};
            property.versioned = {};
            property.current = angular.copy(original);
            property.systemName = property.current.systemName;
            property.displayName = property.current.displayName;
            property.description = property.current.description;
            property.op = operation;
            return property;
        };
        ;
        FeedAdditionalPropertiesController.prototype.findVersionedUserProperty = function (property) {
            var versionedProperty = _.find(this.versionFeedModel.userProperties, function (p) {
                return p.systemName === property.systemName;
            });
            if (versionedProperty === undefined) {
                versionedProperty = {};
            }
            return versionedProperty;
        };
        ;
        FeedAdditionalPropertiesController.prototype.diff = function (path) {
            return this.FeedService.diffOperation(path);
        };
        ;
        FeedAdditionalPropertiesController.prototype.diffCollection = function (path) {
            return this.FeedService.diffCollectionOperation(path);
        };
        ;
        return FeedAdditionalPropertiesController;
    }());
    exports.FeedAdditionalPropertiesController = FeedAdditionalPropertiesController;
    angular.module(moduleName).controller('FeedAdditionalPropertiesController', ["$scope", "$q", "AccessControlService", "EntityAccessControlService", "FeedService", "FeedTagService", "FeedSecurityGroups", "$filter", FeedAdditionalPropertiesController]);
    angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
});
//# sourceMappingURL=feed-additional-properties.js.map