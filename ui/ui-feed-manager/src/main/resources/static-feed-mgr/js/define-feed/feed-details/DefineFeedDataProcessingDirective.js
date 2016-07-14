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


        this.mergeStrategies = [{name:'Sync',type:'SYNC',hint:'Replaces table data',disabled:false},{name:'Merge',type:'MERGE',hint:'Append table data',disabled:false},{name:'Dedupe and Merge',type:'DEDUPE_AND_MERGE',hint:'Append data without duplicates',disabled:false}];
        this.permissionGroups = ['Marketing','Human Resources','Administrators','IT'];

       // this.compressionOptions = ['NONE','SNAPPY','ZLIB'];

        this.allCompressionOptions = {'ORC':['NONE','SNAPPY','ZLIB'],'PARQUET':['NONE','SNAPPY']};

        this.targetFormatOptions = [{label:"ORC",value:'STORED AS ORC'},{label:"PARQUET",value:'STORED AS PARQUET'}];

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

        // Initialize UI
        this.onTableFormatChange();
    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedDataProcessingController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedDataProcessing', directive);


})();




