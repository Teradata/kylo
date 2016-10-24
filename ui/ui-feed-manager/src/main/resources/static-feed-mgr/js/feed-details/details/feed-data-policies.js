(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-data-policies.html',
            controller: "FeedDataPoliciesController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $mdDialog, $timeout, AccessControlService, FeedService, FeedSecurityGroups) {

        var self = this;

        /**
         * Indicates if the feed data policies may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        this.model = FeedService.editFeedModel;
        /**
         * The form for angular errors
         * @type {{}}
         */
        this.editFeedDataPoliciesForm = {};

        this.editableSection = false;

        $scope.$watch(function () {
            return FeedService.editFeedModel;
        }, function (newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
                populateFieldNameMap();
                applyDefaults();

            }
        });

        /**
         * apply default values to the read only model
         */
        function applyDefaults() {
            if (self.model.table.targetFormat === undefined || self.model.table.targetFormat === '' || self.model.table.targetFormat === null) {
                //default to ORC
                self.model.table.targetFormat = 'STORED AS ORC'
            }
            if (self.model.table.options.compressionFormat === undefined || self.model.table.options.compressionFormat === '' || self.model.table.options.compressionFormat === null) {
                self.model.table.options.compressionFormat = 'NONE'
            }
        }

        self.fieldNameMap = {};

        function populateFieldNameMap() {
            self.fieldNameMap = {};

            _.each(self.model.table.tableSchema.fields, function (field) {
                self.fieldNameMap[field.name] = field;
            });
        }

        populateFieldNameMap();
        applyDefaults();

        this.permissionGroups = ['Marketing', 'Human Resources', 'Administrators', 'IT'];
        this.compressionOptions = FeedService.allCompressionOptions();

        this.mergeStrategies = angular.copy(FeedService.mergeStrategies);

        this.targetFormatOptions = FeedService.targetFormatOptions;

        this.feedSecurityGroups = FeedSecurityGroups;

        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;
        self.securityGroupsEnabled = false;

        FeedSecurityGroups.isEnabled().then(function(isValid) {
                self.securityGroupsEnabled = isValid;
            }

        );

        this.fieldDataTypeDisplay = function (columnDef) {
            if (columnDef != undefined) {
                return FeedService.getDataTypeDisplay(columnDef);
            }

        }


        this.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        }

        self.editModel = {};

        function findProperty(key) {
            return _.find(self.model.inputProcessor.properties, function (property) {
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        }

        /**
         * Returns the readable display name for the mergeStrategy on the edited feed model
         * @returns {*}
         */
        this.mergeStrategyDisplayName = function () {
            var mergeStrategyObject = _.find(FeedService.mergeStrategies, function (strategy) {
                return strategy.type == self.model.table.targetMergeStrategy;
            });
            return mergeStrategyObject != null ? mergeStrategyObject.name : self.model.table.targetMergeStrategy
        }

        /**
         * Enable/Disable the PK Merge Strategy
         */
        this.onChangePrimaryKey = function () {
            validateMergeStrategies();
        }

        this.onChangeMergeStrategy = function () {
            validateMergeStrategies();
        }

        function validateMergeStrategies() {
            var valid = FeedService.enableDisablePkMergeStrategy(self.editModel, self.mergeStrategies);
            self.editFeedDataPoliciesForm['targetMergeStrategy'].$setValidity('invalidPKOption', valid);
        }

        this.onEdit = function () {
            //copy the model
            var fieldPolicies = angular.copy(FeedService.editFeedModel.table.fieldPolicies);
            var fields = angular.copy(FeedService.editFeedModel.table.tableSchema.fields);
            //assign the field to the policy
            var fieldMap = _.groupBy(fields, function (field) {
                return field.name
            });
            _.each(fieldPolicies, function (policy) {
                var columnDef = fieldMap[policy.name][0];
                policy.columnDef = columnDef;
            });

            self.editModel = {};
            self.editModel.fieldPolicies = fieldPolicies;

            self.editModel.table = {};
            self.editModel.table.tableSchema = {};
            self.editModel.table.tableSchema.fields = fields;
            self.editModel.table.targetFormat = FeedService.editFeedModel.table.targetFormat;
            if (self.editModel.table.targetFormat === undefined) {
                //default to ORC
                self.editModel.table.targetFormat = 'ORC'
            }
            self.editModel.table.targetMergeStrategy = FeedService.editFeedModel.table.targetMergeStrategy;
            self.editModel.table.options = angular.copy(FeedService.editFeedModel.table.options);
            if (self.editModel.table.options.compressionFormat === undefined) {
                self.editModel.options.compressionFormat = 'NONE'
            }
            self.editModel.securityGroups = angular.copy(FeedService.editFeedModel.securityGroups);
            if (self.editModel.securityGroups == undefined) {
                self.editModel.securityGroups = [];
            }
            $timeout(validateMergeStrategies, 400);
        }

        this.onCancel = function () {

        }

        this.onSave = function (ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving Feed " + self.model.feedName, self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.table.targetFormat = self.editModel.table.targetFormat;
            copy.table.fieldPolicies = self.editModel.fieldPolicies;


            //add back in the changes to the pk, nullable, created, updated tracker columns
            var policyMap = _.groupBy(copy.table.fieldPolicies, function (policy) {
                return policy.name
            });
            _.each(copy.table.tableSchema.fields, function (field) {
                //find the respective changes in the ui object for this field
                var updatedColumnDef = policyMap[field.name] != undefined ? policyMap[field.name][0] : undefined;
                if (updatedColumnDef) {
                    var def = updatedColumnDef.columnDef;
                    angular.extend(field, def);
                }
            });
            //strip off the added 'columnDef' property
            _.each(self.editModel.fieldPolicies, function (policy) {
                policy.columnDef = undefined;
            });

            copy.table.targetMergeStrategy = self.editModel.table.targetMergeStrategy;
            copy.table.options = self.editModel.table.options;
            copy.securityGroups = self.editModel.securityGroups;
            copy.userProperties = null;

            FeedService.saveFeedModel(copy).then(function (response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.table.tableSchema.fields = copy.table.tableSchema.fields;
                self.model.table.targetFormat = self.editModel.table.targetFormat;
                self.model.table.fieldPolicies = self.editModel.fieldPolicies;
                self.model.table.targetMergeStrategy = self.editModel.table.targetMergeStrategy;
                self.model.table.options = self.editModel.table.options;
                self.model.securityGroups = self.editModel.securityGroups;
                populateFieldNameMap();
            }, function (response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        }

        this.showFieldRuleDialog = function (field, policyParam) {
            $mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed:self.model,
                    field: field,
                    policyParameter: policyParam
                }
            })
                .then(function (msg) {

                }, function () {

                });
        };

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions);
                });
    };

    angular.module(MODULE_FEED_MGR).controller('FeedDataPoliciesController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedDataPolicies', directive);

})();
