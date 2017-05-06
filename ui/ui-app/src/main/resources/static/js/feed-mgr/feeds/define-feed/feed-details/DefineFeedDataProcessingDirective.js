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


        var checkAll = {
            isChecked:true,
            isIndeterminate: false,
            totalChecked:0,
            clicked:function(checked){
                if(checked){
                    this.totalChecked++;
                }
                else {
                    this.totalChecked--;
                }
                this.markChecked();
            },
            markChecked:function(){
                if(this.totalChecked == self.model.table.fieldPolicies.length){
                    this.isChecked = true;
                    this.isIndeterminate = false;
                }
                else if(this.totalChecked >0) {
                    this.isChecked = false;
                    this.isIndeterminate = true;
                }
                else if(this.totalChecked == 0){
                    this.isChecked = false;
                    this.isIndeterminate = false;
                }
            }
        }

        /**
         * Toggle Check All/None on Profile column
         * Default it to true
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
         */
        this.profileCheckAll = angular.extend({
            isChecked:true,
            isIndeterminate: false,
            toggleAll: function() {
            var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
                _.each(self.model.table.fieldPolicies,function(field) {
                    field.profile = checked;
                });
                if(checked){
                    this.totalChecked = self.model.table.fieldPolicies.length;
                }
                else {
                    this.totalChecked = 0;
                }
                this.markChecked();
            },
            setup:function(){
                 self.profileCheckAll.totalChecked = 0;
                _.each(self.model.table.fieldPolicies,function(field) {
                    if(field.profile){
                        self.profileCheckAll.totalChecked++;
                    }
                });
                self.profileCheckAll.markChecked();
            }
        },checkAll);


        /**
         *
         * Toggle check all/none on the index column
         *
         * @type {{isChecked: boolean, isIndeterminate: boolean, toggleAll: controller.indexCheckAll.toggleAll}}
         */
        this.indexCheckAll = angular.extend({
            isChecked:false,
            isIndeterminate: false,
            toggleAll: function() {
                var checked = (!this.isChecked || this.isIndeterminate) ? true : false;
                _.each(self.model.table.fieldPolicies,function(field) {
                    field.index = checked;
                });
                this.isChecked = checked;

                if(checked){
                    this.totalChecked = self.model.table.fieldPolicies.length;
                }
                else {
                    this.totalChecked = 0;
                }
                this.markChecked();
            },
            setup:function(){
                self.indexCheckAll.totalChecked = 0;
                _.each(self.model.table.fieldPolicies,function(field) {
                    if(field.index){
                        self.indexCheckAll.totalChecked++;
                    }
                });
                self.indexCheckAll.markChecked();
            }
        },checkAll);




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

                self.profileCheckAll.setup();

                self.indexCheckAll.setup();

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

        this.showFieldRuleDialog = function(field) {
            $mdDialog.show({
                controller: 'FeedFieldPolicyRuleDialogController',
                templateUrl: 'js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:false,
                fullscreen: true,
                locals : {
                    feed: self.model,
                    field:field
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




