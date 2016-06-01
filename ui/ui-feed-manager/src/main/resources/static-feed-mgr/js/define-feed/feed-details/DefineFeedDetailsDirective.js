
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require:['thinkbigDefineFeedDetails','^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/define-feed/feed-details/define-feed-details.html',
            controller: "DefineFeedDetailsController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope, $http,$mdToast,RestUrlService,FeedTagService, FeedService,RegisterTemplateService,FeedInputProcessorOptionsFactory, BroadcastService, StepperService) {

        function DefineFeedDetailsControllerTag() {
        }

        this.__tag = new DefineFeedDetailsControllerTag();

        var self = this;
        this.stepNumber = parseInt(this.stepIndex)+1
        this.model = FeedService.createFeedModel;

        this.inputProcessors = [];

        this.inputProcessorId = null;

        this.isValid = false;

        this.stepperController = null;
        var watchers = [];

        this.codemirrorRenderTypes = RegisterTemplateService.codemirrorRenderTypes;

        var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"]


        BroadcastService.subscribe($scope,StepperService.ACTIVE_STEP_EVENT,onActiveStep)

        function onActiveStep(event,index){
            if(index == parseInt(self.stepIndex)) {
                validate();
            }
        }


        function groupProperties(properties){
            var processors = {};
            //merge in any data that is saved on the model for the processors
            var savedProperties = {};
            angular.forEach(self.model.properties,function(property) {
                if(property.userEditable && property.templateProperty){
                    savedProperties[property.templateProperty.idKey] = property;
                }
            });

      angular.forEach(properties,function(property){

                    //set the value if its saved
                    if(savedProperties[property.idKey] != undefined){
                        property.value == savedProperties[property.idKey]
                    }

                    //mark as not selected
                    property.selected = false;
                    if (processors[property.processorId] === undefined) {
                        processors[property.processorId] = {
                            name: property.processorName,
                            properties: [],
                            processorId: property.processorId,
                            inputProcessor: property.inputProcessor,
                            type: property.processorType
                        }
                    }


                    processors[property.processorId].properties.push(property);
                    if (property.inputProperty) {
                        processors[property.processorId].inputProcessor = true;
                    }
                property.value = RegisterTemplateService.deriveExpression(property.value, false);
                property.renderWithCodeMirror = RegisterTemplateService.isRenderPropertyWithCodeMirror(property);

            });

            var processorDataArray = [];

            var inputProcessors = [];
            var nonInputProcessors = [];
            for(var processorId in processors) {
                processorDataArray.push( processors[processorId]);
                if(processors[processorId].inputProcessor) {
                    inputProcessors.push(processors[processorId]);
                }
                else {
                    nonInputProcessors.push(processors[processorId]);
                }
                angular.forEach(processors[processorId].properties,function(property) {
                    //if it is a custom render property then dont allow the default editing.
                    //the other fields are coded to look for these specific properties
                    //otherwise check to see if it is editable
                    if(FeedService.isCustomPropertyRendering(property.key)){
                        property.customProperty = true;
                        property.userEditable = false;
                    } else if(property.userEditable == true){
                        processors[processorId].userEditable = true;
                        return false;
                    }
                })
                //set userEditable at processor level
            }
            self.inputProcessors = inputProcessors;
            self.model.nonInputProcessors = nonInputProcessors;
            if(self.inputProcessorId == null && self.inputProcessors != null && self.inputProcessors.length >0){
                self.inputProcessorId  = self.inputProcessors[0].processorId;
            }
        }

        /**
         * gets the Registered Template
         * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
         * @returns {HttpPromise}
         */
        function getRegisteredTemplate() {
            if(self.model.templateId != null && self.model.templateId != '') {
                var successFn = function (response) {
                    groupProperties(response.data.properties);
                }
                var errorFn = function (err) {

                }
                var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATE_URL(self.model.templateId));
                promise.then(successFn, errorFn);
                return promise;
            }
        }

        /**
         * Validates the step for enable/disable of the step and continue button
         */
        function validate(){
            var valid = self.model.systemFeedName != '' && self.model.systemFeedName != null && self.model.templateId != null && (self.inputProcessors.length ==0 || (self.inputProcessors.length >0 && self.inputProcessorId != null));
            self.isValid = valid;
        }

        var inputProcessorIdWatch = $scope.$watch(function(){
            return self.inputProcessorId;
        },function(newVal) {
            updateInputProcessor(newVal);
            validate();
        });


        var systemFeedNameWatch = $scope.$watch(function(){
            return self.model.systemFeedName;
        },function(newVal) {
            validate();
        });

      var templateIdWatch =  $scope.$watch(function(){
            return self.model.templateId;
        },function(newVal) {
            getRegisteredTemplate();
        })

        /*
        var inputProcessorTypeWatch = $scope.$watch(function(){
            return self.model.inputProcessorType
        },function(newVal,oldVal){
            if(oldVal != null) {
                if (_.contains(inputDatabaseType, oldVal)) {
                    //reset the table model
                    FeedService.clearTableData();
                }
            }
        })
        */


        function setInputProcessorFeedPropertiesUrl(processor){
            if(processor.feedPropertiesUrl == undefined){
                processor.feedPropertiesUrl = null;
            }
            if(processor.feedPropertiesUrl == null ) {
                processor.feedPropertiesUrl  = FeedInputProcessorOptionsFactory.templateForProcessor(processor,'create');

            }
        }


        function updateInputProcessor(newVal){
            angular.forEach(self.inputProcessors,function(processor) {
                if(processor.processorId == newVal){
                    //check the type and return the custom form if there is one via a factory
                    self.model.table.method = 'MANUAL';
                    self.model.inputProcessor = processor;
                    self.model.inputProcessorType = processor.type;
                    setInputProcessorFeedPropertiesUrl(processor);
                    return false;
                }

            })
        }

        $scope.$on('$destroy',function(){
    systemFeedNameWatch();
    templateIdWatch();
    inputProcessorIdWatch();
  //  inputProcessorTypeWatch();
})

    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedDetailsController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedDetails', directive);

})();
