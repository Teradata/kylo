(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            controllerAs: 'vm',
            require:['thinkbigDefineFeedDataProcessing','^thinkbigStepper'],
            scope: {},
            templateUrl: 'js/define-feed/feed-details/define-feed-data-processing.html',
            controller: "DefineFeedDataProcessingController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope, $http,$mdDialog,RestUrlService, FeedService,FeedSecurityGroups) {
        this.isValid = true;

        var self = this;
        this.model = FeedService.createFeedModel;
        this.feedSecurityGroups = FeedSecurityGroups;
        this.stepNumber = parseInt(this.stepIndex)+1


        this.tableTypes = [{name:'Snapshot',type:'SNAPSHOT',hint:'Snapshot and overwrite table'},{name:'Delta',type:'DELTA',hint:'Merges content into existing table'}];
        this.permissionGroups = ['Marketing','Human Resources','Administrators','IT'];

        this.compressionOptions = ['NONE','SNAPPY','ZLIB'];

        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;

        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip }
        }


        this.filterFieldDates = function(field){
            return field.dataType == 'date' || field.dataType == 'timestamp';
        }

        this.INCREMENTAL_DATE_FIELD_KEY = 'Date Field';


        function findIncrementalDateFieldProperty(){
            return findProperty(self.INCREMENTAL_DATE_FIELD_KEY);
        }

        this.onIncrementalDateFieldChange = function(){
            var prop = findIncrementalDateFieldProperty();
            if(prop != null) {
             prop.value =  self.model.table.incrementalDateField;
            }
        }

        function findProperty(key){
            return _.find(self.model.inputProcessor.properties,function(property){
                //return property.key = 'Source Database Connection';
                return property.key == key;
            });
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


    angular.module(MODULE_FEED_MGR).controller('DefineFeedDataProcessingController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedDataProcessing', directive);


})();




