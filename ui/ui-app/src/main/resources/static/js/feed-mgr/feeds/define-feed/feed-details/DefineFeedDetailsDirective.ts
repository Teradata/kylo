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
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

export class DefineFeedDetailsController {

    /**
     * The Angular Form for validation
     * @type {{}}
     */
    feedDetailsForm: any = {};

    stepNumber: number;
    model: any;

    inputProcessors: any;

    inputProcessorId: any = null;

    /**
     * The initial non null input Id selected
     * @type {null}
     */
    initialInputProcessorId: any;

    /**
     * counter holding the number of times the user changes to a different input
     * @type {number}
     */
    inputChangedCounter: number = 0;

    isValid: boolean = false;


    /**
     * flag to indicate if the data is still loading
     * @type {boolean}
     */
    loading: boolean = false;

    codemirrorRenderTypes: any;
    stepIndex: any;
    stepperController: any = null;
    totalSteps: number;
    inputProcessor: any = [];
    nonInputProcessors: any;

    $onInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }


    static readonly $inject = ["$scope", "$http", "RestUrlService", "FeedService", "RegisterTemplateService",
        "FeedInputProcessorOptionsFactory", "BroadcastService", "StepperService", "FeedDetailsProcessorRenderingHelper"];

    constructor(private $scope: any, private $http: any, private RestUrlService: any, private FeedService: any, private RegisterTemplateService: any
        , private FeedInputProcessorOptionsFactory: any, private BroadcastService: any, private StepperService: any,
        private FeedDetailsProcessorRenderingHelper: any) {

        this.model = FeedService.createFeedModel;

        var watchers = [];

        this.codemirrorRenderTypes = RegisterTemplateService.codemirrorRenderTypes;

        var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"]

        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, (event: any, index: any) => {
            if (index == parseInt(this.stepIndex)) {
                this.validate();
            }
        });

        var inputProcessorIdWatch = $scope.$watch(function () {
            return this.inputProcessorId;
        }, function (newVal: any, oldVal: any) {
            if (newVal != null && this.initialInputProcessorId == null) {
                this.initialInputProcessorId = newVal;
            }
            this.updateInputProcessor(newVal);
            //mark the next step as not visited.. force the user to go to the next step
            this.stepperController.resetStep(parseInt(this.stepIndex) + 1);
            this.validate();
        });

        var systemFeedNameWatch = $scope.$watch(function () {
            return this.model.systemFeedName;
        }, function (newVal: any) {
            this.validate();
        });

        var templateIdWatch = $scope.$watch(function () {
            return this.model.templateId;
        }, function (newVal: any) {
            this.loading = true;
            this.getRegisteredTemplate();
        });

        $scope.$on('$destroy', function () {
            systemFeedNameWatch();
            templateIdWatch();
            inputProcessorIdWatch();
        })

    }

    /**
         * Finds the allowed controller services for the specified property and sets the allowable values.
         *
         * @param {Object} property the property to be updated
         */
    findControllerServicesForProperty(property: any) {

        this.FeedService.findControllerServicesForProperty(property);
    }

    matchInputProcessor(inputProcessor: any, inputProcessors: any) {

        if (inputProcessor == null) {
            //input processor is null when feed is being created
            return undefined;
        }

        var matchingInput = _.find(inputProcessors, function (input: any) {
            if (input.id == inputProcessor.id) {
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
    initializeProperties(template: any) {
        if (angular.isDefined(this.model.cloned) && this.model.cloned == true) {
            this.RegisterTemplateService.setProcessorRenderTemplateUrl(this.model, 'create');
            var inputProcessors = this.model.inputProcessors;
            this.inputProcessors = _.sortBy(inputProcessors, 'name')
            // Find controller services
            _.chain(this.inputProcessors.concat(this.model.nonInputProcessors))
                .pluck("properties")
                .flatten(true)
                .filter(function (property) {
                    return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                })
                .each(this.findControllerServicesForProperty);

        } else {
            this.RegisterTemplateService.initializeProperties(template, 'create', this.model.properties);
            var inputProcessors = this.RegisterTemplateService.removeNonUserEditableProperties(template.inputProcessors, true);
            //sort them by name
            this.inputProcessors = _.sortBy(inputProcessors, 'name')

            this.model.allowPreconditions = template.allowPreconditions;

            this.model.nonInputProcessors = this.RegisterTemplateService.removeNonUserEditableProperties(template.nonInputProcessors, false);
        }
        if (angular.isDefined(this.model.inputProcessor)) {
            var match = this.matchInputProcessor(this.model.inputProcessor, this.inputProcessors);
            if (angular.isDefined(match)) {
                this.inputProcessor = match;
                this.inputProcessorId = match.id;
            }
        }

        if (this.inputProcessorId == null && this.inputProcessors != null && this.inputProcessors.length > 0) {
            this.inputProcessorId = this.inputProcessors[0].id;
        }
        // Skip this step if it's empty
        if (this.inputProcessors.length === 0 && !_.some(this.nonInputProcessors, function (processor: any) {
            return processor.userEditable
        })) {
            var step = this.StepperService.getStep("DefineFeedStepper", parseInt(this.stepIndex));
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
            .each(this.findControllerServicesForProperty);

        this.loading = false;
        this.validate();
    }

    /**
         * gets the Registered Template
         * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
         * @returns {HttpPromise}
         */
    getRegisteredTemplate() {
        if (this.model.templateId != null && this.model.templateId != '') {
            var successFn = function (response: any) {
                this.initializeProperties(response.data);
            };
            var errorFn = function (err: any) {

            };
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(this.model.templateId), { params: { feedEdit: true, allProperties: true } });
            promise.then(successFn, errorFn);
            return promise;
        }
    }

    /**
         * Validates the step for enable/disable of the step and continue button
         */
    validate() {
        this.isValid = this.loading == false && this.model.systemFeedName != '' && this.model.systemFeedName != null && this.model.templateId != null
            && (this.inputProcessors.length == 0 || (this.inputProcessors.length > 0 && this.inputProcessorId != null));
    }

    /**
         * Updates the details when the processor is changed.
         *
         * @param {string} processorId the processor id
         */
    updateInputProcessor(processorId: any) {
        // Find the processor object
        var processor = _.find(this.inputProcessors, function (processor: any) {
            return (processor.processorId === processorId);
        });
        if (angular.isUndefined(processor)) {
            return;
        }

        this.inputChangedCounter++;

        var clonedAndInputChanged = function (inputProcessorId: any) {
            return (this.model.cloned == true && this.inputChangedCounter > 1);
        }

        var notCloned = function () {
            return (angular.isUndefined(this.model.cloned) || this.model.cloned == false);
        }

        // Determine render type
        var renderGetTableData = this.FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, this.model.nonInputProcessors);

        if (renderGetTableData) {

            if (this.model.table.method != null && this.model.table.method != 'EXISTING_TABLE') {
                //reset the fields
                this.model.table.tableSchema.fields = [];
            }
            this.model.table.method = 'EXISTING_TABLE';
            this.model.options.skipHeader = true;
            this.model.allowSkipHeaderOption = true;

        } else if (this.model.templateTableOption != "DATA_TRANSFORMATION" && (clonedAndInputChanged(processorId) || notCloned())) {
            this.model.table.method = 'SAMPLE_FILE';
            this.model.table.tableSchema.fields = [];
        }

        // Update model
        this.model.inputProcessor = processor;
        this.model.inputProcessorType = processor.type;
    }

}


angular.module(moduleName).
    component("thinkbigDefineFeedDetails", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"

        },
        controllerAs: 'vm',
        controller: DefineFeedDetailsController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-details.html',
    });