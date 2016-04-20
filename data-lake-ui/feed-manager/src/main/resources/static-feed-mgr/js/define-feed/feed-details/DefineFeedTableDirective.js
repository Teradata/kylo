(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            require:['thinkbigDefineFeedTable','^thinkbigStepper'],
            controllerAs: 'vm',
            templateUrl: 'js/define-feed/feed-details/define-feed-table.html',
            controller: "DefineFeedTableController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope, $http,$mdToast,RestUrlService, FeedService, FileUpload) {


        function DefineFeedTableController() {
        }

        this.__tag = new DefineFeedTableController();

        var self = this;
        this.stepNumber = parseInt(this.stepIndex)+1
        this.stepperController = null;
        this.model = FeedService.createFeedModel;
        this.isValid = false;
        this.sampleFile = null;
        this.tableCreateMethods = [{type:'MANUAL',name:'Manual'},{type:'SAMPLE_TABLE',name:'Sample Table'},{type:'SAMPLE_FILE',name:'Sample File'}];
        this.tableRecordFormats = [{type:'DELIMITED',name:'Delimited',hint:'Data Files use delimiters like commas (CSV) or tabs'},{type:'SERDE',name:'Serde',hint:'Enter a specialized serialization implementation'}]
        this.columnDefinitionDataTypes = ['string','int','bigint','tinyint','double','float','date','timestamp','boolean','binary'];

        this.uploadBtnDisabled = false;
        function newColumnDefinition() {
            return FeedService.newTableFieldDefinition();
        }
        function newColumnPolicy() {
            return FeedService.newTableFieldPolicy();
        }

        function newPartitionField(index) {
            return {position:index,field:'',sourceField:'',formula:''}
        }

        this.partitionFormulas = ['val','year','month','day','hour','min','sec'];


        function showProgress(){
            if(self.stepperController) {
                self.stepperController.showProgress = true;
            }
        }

        function hideProgress(){
            if(self.stepperController) {
                self.stepperController.showProgress = false;
            }
        }


       function resetColumns() {
           self.model.table.tableSchema.fields = [];
           self.model.table.fieldPolicies = [];
       }




        this.addColumn = function(columnDef) {
            if(columnDef == null){
                columnDef = newColumnDefinition();
            }
            if(columnDef.sampleValues != null && columnDef.sampleValues.length >0){
                columnDef.selectedSampleValue = columnDef.sampleValues[0];
            }
            else {
                columnDef.selectedSampleValue = null;
            }
            self.model.table.tableSchema.fields.push(columnDef);
            self.model.table.fieldPolicies.push(newColumnPolicy())
            self.model.table.sourceTableSchema.fields.push(newColumnDefinition());
        }
        this.removeColumn = function(index) {
            self.model.table.tableSchema.fields.splice(index,1);
            self.model.table.sourceTableSchema.fields.splice(index,1);
        }


        this.addPartitionField = function() {
            var partitionLength = self.model.table.partitions.length;
            var partition = newPartitionField(partitionLength);
            self.model.table.partitions.push(partition);
        }

        this.removePartitionField = function(index) {
            self.model.table.partitions.splice(index,1);
        }


        function validate(){
           var valid =  self.model.templateId != null && self.model.table.method != null && self.model.table.tableSchema.name != null && self.model.table.tableSchema.name != '' && self.model.table.tableSchema.fields.length >0;
            if(valid){
                angular.forEach(self.model.table.tableSchema.fields,function(columnDef){
                    if(columnDef.name == '' || columnDef.name == null){
                        valid = false;
                        return false;
                    }
                    if(columnDef.dataType == '') {
                        valid = false;
                        return false;
                    }
                });
            }
            self.isValid = valid;
        };


        //Set the Table Name to be the System Feed Name
        var systemFeedNameWatch = $scope.$watch(function(){
            return self.model.systemFeedName;
        },function(newVal) {
            self.model.table.tableSchema.name = newVal;
            validate();
        });

      var tableMethodWatch =  $scope.$watch(function() {
            return self.model.table.method;
        }, function(newVal) {
            validate();
        });

        var tableFieldsWatch = $scope.$watch(function() {
            return self.model.table.tableSchema.fields;
        }, function(newVal) {
            validate();
               FeedService.syncTableFieldPolicyNames();
        },true);



        this.uploadSampleFile = function(){
            self.uploadBtnDisabled = true;
            showProgress();
            var file = self.sampleFile;
            var uploadUrl = RestUrlService.UPLOAD_SAMPLE_TABLE_FILE;
            var successFn = function(response) {
                resetColumns();
                angular.forEach(response.fields,function(field) {
                    self.addColumn(field);
                });
                hideProgress();

                self.uploadBtnDisabled = false;
            }
            var errorFn = function(data){
                hideProgress();
                self.uploadBtnDisabled = false;
            }
            FileUpload.uploadFileToUrl(file, uploadUrl,successFn,errorFn);
        };

        $scope.$on('$destroy',function() {
            systemFeedNameWatch();
            tableMethodWatch();
            tableFieldsWatch();
        })


    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedTableController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedTable', directive);

    angular.module(MODULE_FEED_MGR).filter('filterPartitionFormula',['FeedService',function(FeedService) {
      return function(formulas, partition) {
            if(partition && partition.sourceField) {
                var columnDef = FeedService.getColumnDefinitionByName(partition.sourceField);
                 if(columnDef){
                    return _.filter(formulas,function(formula) {
                        if(columnDef.dataType != 'date' && formula !='val') {
                            return false;
                        }
                        else {
                            return true;
                        }
                    })

                }
                else {
                    return formulas;
                }
            }
            else {
                return formulas;
            }
        };
    }]);

})();
