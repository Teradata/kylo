/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-data-policies.html',
            controller: "FeedDataPoliciesController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope, $mdDialog,FeedService,FeedSecurityGroups) {

        var self = this;

        this.model = FeedService.editFeedModel;


        $scope.$watch(function () {
            return FeedService.editFeedModel;
        }, function (newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        })



        this.permissionGroups = ['Marketing','Human Resources','Administrators','IT'];
        this.compressionOptions = ['NONE','SNAPPY','ZLIB'];


        this.mergeStrategies = [{name:'Sync',type:'SYNC',hint:'Sync and overwrite table',disabled:false},{name:'Merge',type:'MERGE',hint:'Merges content into existing table',disabled:false},{name:'Dedupe and Merge',type:'DEDUPE_MERGE',hint:'Dedupe and Merge content into existing table',disabled:false}];

        this.targetFormatOptions = [{label:"ORC",value:'STORED AS ORC'},{label:"PARQUET",value:'STORED AS PARQUET'}];



        this.feedSecurityGroups = FeedSecurityGroups;

        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;

        this.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        }


        self.editModel = {};

        function findProperty(key){
            return _.find(self.model.inputProcessor.properties,function(property){
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        }


        this.onEdit = function () {
            //copy the model
            var fieldPolicies = angular.copy(FeedService.editFeedModel.table.fieldPolicies);

            self.editModel = {};
            self.editModel.fieldPolicies = fieldPolicies;

            self.editModel.table = {};
            self.editModel.table.targeFormat = FeedService.editFeedModel.table.targetFormat;
            self.editModel.table.targetMergeStrategy = FeedService.editFeedModel.table.targetMergeStrategy;
            self.editModel.table.options = angular.copy(FeedService.editFeedModel.table.options);
            self.editModel.table.securityGroups = angular.copy(FeedService.editFeedModel.table.securityGroups);
            if (self.editModel.table.securityGroups == undefined){
            self.editModel.table.securityGroups = [];
            }
        }

        this.onCancel = function() {

        }
        this.onSave = function() {
            //save changes to the model
            self.model.table.targeFormat = self.editModel.table.targeFormat;
            self.model.table.fieldPolicies = self.editModel.fieldPolicies;
            self.model.table.targetMergeStrategy = self.editModel.table.targetMergeStrategy;
            self.model.table.options = self.editModel.table.options;
            self.model.table.securityGroups = self.editModel.table.securityGroups;

            FeedService.saveFeedModel(self.model);
        }

        this.showFieldRuleDialog = function(field,policyParam) {
            $mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    field:field,
                    policyParameter:policyParam
                }
            })
                .then(function(msg) {


                }, function() {

                });
        };

    };


    angular.module(MODULE_FEED_MGR).controller('FeedDataPoliciesController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedDataPolicies', directive);

})();
