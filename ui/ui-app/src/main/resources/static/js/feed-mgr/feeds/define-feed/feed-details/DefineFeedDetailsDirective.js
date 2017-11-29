/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigDefineFeedDetails', '^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-details.html',
            controller: "DefineFeedDetailsController",
            link: function($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        };
    };

    function DefineFeedDetailsController($scope, $http, RestUrlService, FeedService, RegisterTemplateService, FeedInputProcessorOptionsFactory, BroadcastService, StepperService,
                                         FeedDetailsProcessorRenderingHelper) {
        var self = this;

        /**
         * The Angular Form for validation
         * @type {{}}
         */
        this.feedDetailsForm = {};

        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.model = FeedService.createFeedModel;

        this.inputProcessors = [];

        this.inputProcessorId = null;

        /**
         * The initial non null input Id selected
         * @type {null}
         */
        this.initialInputProcessorId = null;

        /**
         * counter holding the number of times the user changes to a different input
         * @type {number}
         */
        this.inputChangedCounter = 0;

        this.isValid = false;

        this.stepperController = null;

        /**
         * flag to indicate if the data is still loading
         * @type {boolean}
         */
        this.loading = false;

        var watchers = [];

        this.codemirrorRenderTypes = RegisterTemplateService.codemirrorRenderTypes;

        var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"]

        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, onActiveStep)

        function onActiveStep(event, index) {
            if (index == parseInt(self.stepIndex)) {
                validate();
            }
        }

        /**
         * Finds the allowed controller services for the specified property and sets the allowable values.
         *
         * @param {Object} property the property to be updated
         */
        function findControllerServicesForProperty(property) {

            FeedService.findControllerServicesForProperty(property);
        }

        function matchInputProcessor(inputProcessor, inputProcessors){

            if (inputProcessor == null) {
                //input processor is null when feed is being created
                return undefined;
            }

           var matchingInput = _.find(inputProcessors,function(input) {
                if(input.id == inputProcessor.id){
                    return true;
                }
               return (input.type == inputProcessor.type && input.name == inputProcessor.name);
            });

           return matchingInput;
        }

        /**
         * Prepares the processor properties of the specified template for display.
         *
         * @param {Object} template the template with properties
         */
        function initializeProperties(template) {
            if(angular.isDefined(self.model.cloned) && self.model.cloned == true) {
                RegisterTemplateService.setProcessorRenderTemplateUrl(self.model, 'create');
               var inputProcessors = self.model.inputProcessors;
                self.inputProcessors = _.sortBy(inputProcessors,'name')

            }else {
                RegisterTemplateService.initializeProperties(template, 'create', self.model.properties);
                var inputProcessors = RegisterTemplateService.removeNonUserEditableProperties(template.inputProcessors, true);
                //sort them by name
                self.inputProcessors = _.sortBy(inputProcessors, 'name')

                self.model.allowPreconditions = template.allowPreconditions;

                self.model.nonInputProcessors = RegisterTemplateService.removeNonUserEditableProperties(template.nonInputProcessors, false);
            }
                if (angular.isDefined(self.model.inputProcessor)) {
                    var match = matchInputProcessor(self.model.inputProcessor, self.inputProcessors);
                    if (angular.isDefined(match)) {
                        self.inputProcessor = match;
                        self.inputProcessorId = match.id;
                    }
                }

                if (self.inputProcessorId == null && self.inputProcessors != null && self.inputProcessors.length > 0) {
                    self.inputProcessorId = self.inputProcessors[0].id;
                }
                // Skip this step if it's empty
                if (self.inputProcessors.length === 0 && !_.some(self.nonInputProcessors, function (processor) {
                        return processor.userEditable
                    })) {
                    var step = StepperService.getStep("DefineFeedStepper", parseInt(self.stepIndex));
                    if (step != null) {
                        step.skip = true;
                    }
                }

                // Find controller services
                _.chain(template.inputProcessors.concat(template.nonInputProcessors))
                    .pluck("properties")
                    .flatten(true)
                    .filter(function (property) {
                        return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                    })
                    .each(findControllerServicesForProperty);

            self.loading = false;
            validate();
        }

        /**
         * gets the Registered Template
         * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
         * @returns {HttpPromise}
         */
        function getRegisteredTemplate() {
            if (self.model.templateId != null && self.model.templateId != '') {
                var successFn = function(response) {
                    initializeProperties(response.data);
                };
                var errorFn = function(err) {

                };
                var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATE_URL(self.model.templateId),{params:{feedEdit:true,allProperties:true}});
                promise.then(successFn, errorFn);
                return promise;
            }
        }

        /**
         * Validates the step for enable/disable of the step and continue button
         */
        function validate() {
            self.isValid = self.loading == false && self.model.systemFeedName != '' && self.model.systemFeedName != null && self.model.templateId != null
                           && (self.inputProcessors.length == 0 || (self.inputProcessors.length > 0 && self.inputProcessorId != null));
        }

        var inputProcessorIdWatch = $scope.$watch(function() {
            return self.inputProcessorId;
        }, function(newVal,oldVal) {
            if(newVal != null && self.initialInputProcessorId == null){
                self.initialInputProcessorId = newVal;
            }
            updateInputProcessor(newVal);
            //mark the next step as not visited.. force the user to go to the next step
            self.stepperController.resetStep(parseInt(self.stepIndex)+1);
            validate();
        });

        var systemFeedNameWatch = $scope.$watch(function() {
            return self.model.systemFeedName;
        }, function(newVal) {
            validate();
        });

        var templateIdWatch = $scope.$watch(function() {
            return self.model.templateId;
        }, function(newVal) {
            self.loading = true;
            getRegisteredTemplate();
        });



        /**
         * Updates the details when the processor is changed.
         *
         * @param {string} processorId the processor id
         */
        function updateInputProcessor(processorId) {
            // Find the processor object
            var processor = _.find(self.inputProcessors, function(processor) {
                return (processor.processorId === processorId);
            });
            if (angular.isUndefined(processor)) {
                return;
            }

            self.inputChangedCounter++;

            var clonedAndInputChanged = function(inputProcessorId){
                return (self.model.cloned == true && self.inputChangedCounter >1);
            }

            var notCloned = function(){
                return (angular.isUndefined(self.model.cloned) || self.model.cloned == false);
            }

            // Determine render type
            var renderGetTableData = FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, self.model.nonInputProcessors);
          
              if (renderGetTableData) {

                  if(self.model.table.method != null && self.model.table.method != 'EXISTING_TABLE'){
                      //reset the fields
                      self.model.table.tableSchema.fields = [];
                  }
                  self.model.table.method = 'EXISTING_TABLE';
                  self.model.options.skipHeader = true;
                  self.model.allowSkipHeaderOption = true;

              } else if(self.model.templateTableOption !="DATA_TRANSFORMATION" && (clonedAndInputChanged(processorId)|| notCloned())){
                  self.model.table.method = 'SAMPLE_FILE';
                  self.model.table.tableSchema.fields = [];
              }

            // Update model
            self.model.inputProcessor = processor;
            self.model.inputProcessorType = processor.type;
        }

        $scope.$on('$destroy', function() {
            systemFeedNameWatch();
            templateIdWatch();
            inputProcessorIdWatch();
        })

    }

    angular.module(moduleName).controller('DefineFeedDetailsController', ["$scope","$http","RestUrlService","FeedService","RegisterTemplateService","FeedInputProcessorOptionsFactory","BroadcastService","StepperService","FeedDetailsProcessorRenderingHelper",DefineFeedDetailsController]);
    angular.module(moduleName).directive('thinkbigDefineFeedDetails', directive);
});
