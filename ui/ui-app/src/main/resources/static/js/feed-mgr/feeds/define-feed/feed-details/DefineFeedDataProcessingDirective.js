define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            controllerAs: 'vm',
            require:['thinkbigDefineFeedDataProcessing','^thinkbigStepper'],
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-data-processing.html',
            controller: "DefineFeedDataProcessingController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller = function ($scope, $http, $mdDialog, $mdExpansionPanel, RestUrlService, FeedService, BroadcastService, StepperService, Utils) {
        this.isValid = true;

        var self = this;
        this.model = FeedService.createFeedModel;
        this.stepNumber = parseInt(this.stepIndex)+1
        this.selectedColumn = {};

        /**
         * The form in angular
         * @type {{}}
         */
        this.dataProcessingForm = {};


        this.expandFieldPoliciesPanel = function () {
            $mdExpansionPanel().waitFor('panelFieldPolicies').then(function (instance) {
                instance.expand();
            });
        }

        this.mergeStrategies = angular.copy(FeedService.mergeStrategies);
        FeedService.updateEnabledMergeStrategy(self.model, self.mergeStrategies);
        this.permissionGroups = ['Marketing','Human Resources','Administrators','IT'];

        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, onActiveStep)

        function onActiveStep(event, index) {
            if (index == parseInt(self.stepIndex)) {
                validateMergeStrategies();

                // Update the data type display
                _.each(self.model.table.tableSchema.fields, function(columnDef, idx) {
                    columnDef.dataTypeDisplay = FeedService.getDataTypeDisplay(columnDef);
                    var policy = self.model.table.fieldPolicies[idx];
                    policy.name = columnDef.name;
                });

            }
        }

        this.allCompressionOptions = FeedService.compressionOptions;

        this.targetFormatOptions = FeedService.targetFormatOptions;

        // Open panel by default
        self.expandFieldPoliciesPanel();


        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip }
        }

        this.compressionOptions = ['NONE'];

        this.onTableFormatChange = function(opt){

            var format = self.model.table.targetFormat;
            if(format == 'STORED AS ORC' ){
                self.compressionOptions = self.allCompressionOptions['ORC'];
            }
            else  if(format == 'STORED AS PARQUET' ){
                self.compressionOptions = self.allCompressionOptions['PARQUET'];
            }
            else {
                self.compressionOptions = ['NONE'];
            }
        }

        function findProperty(key){
            return _.find(self.model.inputProcessor.properties,function(property){
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
        }

        this.onChangeMergeStrategy = function () {
            validateMergeStrategies();
        }

        function validateMergeStrategies() {
            var validPK = FeedService.enableDisablePkMergeStrategy(self.model, self.mergeStrategies);

            self.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidPKOption', validPK);

            validRollingSync = FeedService.enableDisableRollingSyncMergeStrategy(self.model, self.mergeStrategies);

            self.dataProcessingForm['targetMergeStrategy'].$setValidity('invalidRollingSyncOption', validRollingSync);

            self.isValid = validRollingSync && validPK;
        }

        this.getSelectedColumn = function () {
            return self.selectedColumn;
        };

        this.onSelectedColumn = function (index) {

            var selectedColumn = self.model.table.tableSchema.fields[index];
            var firstSelection = self.selectedColumn == null;
            self.selectedColumn = selectedColumn;

            if(firstSelection){
                //trigger scroll to stick the selection to the screen
                Utils.waitForDomElementReady('#selectedColumnPanel2',function() {
                    angular.element('#selectedColumnPanel2').triggerHandler('stickIt');
                })
            }
        };

        this.showFieldRuleDialog = function(field,policyParam) {
            $mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    feed: self.model,
                    field:field,
                    policyParameter:policyParam
                }
            })
                .then(function(msg) {


                }, function() {

                });
        };

        // Initialize UI
        this.onTableFormatChange();
    };


    angular.module(moduleName).controller('DefineFeedDataProcessingController', ["$scope","$http","$mdDialog","$mdExpansionPanel","RestUrlService","FeedService","BroadcastService","StepperService","Utils",controller]);

    angular.module(moduleName)
        .directive('thinkbigDefineFeedDataProcessing', directive);


})




