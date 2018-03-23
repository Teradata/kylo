import * as angular from 'angular';
import * as _ from 'underscore';
import 'pascalprecht.translate';
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var directive = function() {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
        controller: "FeedAdditionalPropertiesController",
        link: function($scope:any, element:any, attrs:any, controller:any) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
        }
    };
};



export class FeedAdditionalPropertiesController {
// define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {
        versions:any = this.$scope.versions;
        /**
         * Indicates if the feed properties may be edited.
         * @type {boolean}
         */
        allowEdit:boolean = !this.versions;
        
        model:any = this.FeedService.editFeedModel;
        versionFeedModel:any = this.FeedService.versionFeedModel;
        versionFeedModelDiff:any = this.FeedService.versionFeedModelDiff;
        editModel:any = {};
        editableSection:boolean = false;

        feedTagService:any = this.FeedTagService;
        tagChips:any = {};
        securityGroupChips:any = {};
        isValid:boolean = true;
        feedSecurityGroups:any = this.FeedSecurityGroups;
        securityGroupsEnabled:boolean = false;
        userProperties: any = [];
 
        transformChip = function(chip:any) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };

        onEdit = function() {
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
        
        
        onCancel = function() {
            // do nothing
        };

        onSave = function(ev:any) {
            //save changes to the model
            this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-additional-properties.Saving'), this.model.feedName);
            var copy = angular.copy(this.FeedService.editFeedModel);

            copy.tags = this.editModel.tags;
            copy.dataOwner = this.editModel.dataOwner;
            copy.userProperties = this.editModel.userProperties;
            copy.securityGroups = this.editModel.securityGroups;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;

            this.FeedService.saveFeedModel(copy).then((response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.editableSection = false;
                //save the changes back to the model
                this.model.tags = this.editModel.tags;
                this.model.dataOwner = this.editModel.dataOwner;
                this.model.userProperties = this.editModel.userProperties;
                this.model.securityGroups = this.editModel.securityGroups;
                //Get the updated value from the server.
                this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
            }, (response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.FeedService.buildErrorData(this.model.feedName, response);
                this.FeedService.showFeedErrorsDialog();
                //make it editable
                this.editableSection = true;
            });
        };


    constructor (private $scope:any,private $q:any, private AccessControlService:any, private EntityAccessControlService:any,private FeedService:any, private FeedTagService:any, private FeedSecurityGroups:any, private $filter:any) {
        var self = this;

        this.tagChips.selectedItem = null;
        this.tagChips.searchText = null;


        this.securityGroupChips.selectedItem = null;
        this.securityGroupChips.searchText = null;
        


        FeedSecurityGroups.isEnabled().then((isValid:any) => {
                self.securityGroupsEnabled = isValid;
            }

        );

        

        $scope.$watch(() => {
            return FeedService.editFeedModel;
        }, (newVal:any) => {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        });
        
        if (self.versions) {
            $scope.$watch(function(){
                return self.FeedService.versionFeedModel;
            },function(newVal:any) {
                self.versionFeedModel = self.FeedService.versionFeedModel;
            });
            $scope.$watch(function(){
                return self.FeedService.versionFeedModelDiff;
            },function(newVal:any) {
                self.versionFeedModelDiff = self.FeedService.versionFeedModelDiff;

                self.userProperties = [];
                _.each(self.versionFeedModel.userProperties, function(versionedProp) {
                    let property:any = {};
                    property.versioned = angular.copy(versionedProp);
                    property.op = 'no-op';
                    property.systemName = property.versioned.systemName;
                    property.displayName = property.versioned.displayName;
                    property.description = property.versioned.description;
                    property.current = angular.copy(property.versioned);
                    self.userProperties.push(property);
                });
                _.each(_.values(self.versionFeedModelDiff), function(diff){
                    if (diff.path.startsWith("/userProperties")) {
                        if (diff.path.startsWith("/userProperties/")) {
                            //individual versioned indexed action
                            let remainder = diff.path.substring("/userProperties/".length, diff.path.length);
                            let indexOfSlash = remainder.indexOf("/");
                            let versionedPropIdx = remainder.substring(0, indexOfSlash > 0 ? indexOfSlash : remainder.length);
                            if ("replace" === diff.op) {
                                let property = self.userProperties[versionedPropIdx];
                                property.op = diff.op;
                                let replacedPropertyName = remainder.substring(remainder.indexOf("/") + 1, remainder.length);
                                property.current[replacedPropertyName] = diff.value;
                                property[replacedPropertyName] = diff.value;
                            } else if ("add" === diff.op) {
                                if (_.isArray(diff.value)) {
                                    _.each(diff.value, function(prop){
                                        self.userProperties.push(self.createProperty(prop, diff.op));
                                    });
                                } else {
                                    self.userProperties.unshift(self.createProperty(diff.value, diff.op));
                                }
                            } else if ("remove" === diff.op) {
                                let property = self.userProperties[versionedPropIdx];
                                property.op = diff.op;
                                property.current = {};
                            }
                        } else {
                            //group versioned action, can be either "add" or "remove"
                            if ("add" === diff.op) {
                                if (_.isArray(diff.value)) {
                                    _.each(diff.value, function(prop){
                                        self.userProperties.push(self.createProperty(prop, diff.op));
                                    });
                                } else {
                                    self.userProperties.push(self.createProperty(diff.value, diff.op));
                                }
                            } else if ("remove" === diff.op) {
                                _.each(self.userProperties, function(prop:any){
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
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access:any) => {
            self.allowEdit = !self.versions && access && !self.model.view.properties.disabled;
        });
    }

    createProperty(original:any, operation:any) {
        let property:any = {};
        property.versioned = {};
        property.current = angular.copy(original);
        property.systemName = property.current.systemName;
        property.displayName = property.current.displayName;
        property.description = property.current.description;
        property.op = operation;
        return property;
    };


    findVersionedUserProperty(property:any) {
        let versionedProperty = _.find(this.versionFeedModel.userProperties, function(p:any) {
            return p.systemName === property.systemName;
        });
        if (versionedProperty === undefined) {
            versionedProperty = {};
        }
        return versionedProperty;
    };

    diff(path:any) {
        return this.FeedService.diffOperation(path);
    };
    diffCollection(path:any) {
        return this.FeedService.diffCollectionOperation(path);
    };
}
angular.module(moduleName).controller('FeedAdditionalPropertiesController',["$scope","$q","AccessControlService","EntityAccessControlService","FeedService","FeedTagService","FeedSecurityGroups","$filter",FeedAdditionalPropertiesController]);
angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
