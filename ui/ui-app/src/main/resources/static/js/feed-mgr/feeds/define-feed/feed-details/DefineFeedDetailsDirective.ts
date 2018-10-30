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
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import { FeedService } from '../../../services/FeedService';
import {BroadcastService} from '../../../../services/broadcast-service';
import {RegisterTemplatePropertyService} from "../../../services/RegisterTemplatePropertyService";
import {FeedInputProcessorPropertiesTemplateService} from "../../../services/FeedInputProcessorPropertiesTemplateService";
const moduleName = require('../module-name');

export class DefineFeedDetailsController {

    /**
     * The Angular Form for validation
     * @type {{}}
     */
    feedDetailsForm: any = {};

    stepNumber: number;
    model: any;

    inputProcessors: any = [];

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

    stepIndex: any;
    stepperController: any = null;
    totalSteps: number;
    inputProcessor: any = [];
    nonInputProcessors: any;

    inputProcessorIdWatch:any;
    systemFeedNameWatch:any;
    templateIdWatch:any;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
    }

    $onDestroy(){
        this.ngOnDestroy(); 
    }
    ngOnDestroy(){
        this.systemFeedNameWatch();
        this.templateIdWatch();
        this.inputProcessorIdWatch();
    }

    static readonly $inject = ["$scope", "$http", "RestUrlService", "FeedService", "RegisterTemplateService",
        "FeedInputProcessorPropertiesTemplateService", "BroadcastService", "StepperService", "FeedDetailsProcessorRenderingHelper","RegisterTemplatePropertyService"];

    constructor(private $scope: IScope, private $http: angular.IHttpService, private RestUrlService: any, private feedService: FeedService, private registerTemplateService: RegisterTemplateServiceFactory
        , private FeedInputProcessorPropertiesTemplateService: any, private broadcastService: BroadcastService, private StepperService: any,
        private FeedDetailsProcessorRenderingHelper: any,
                private registerTemplatePropertyService:RegisterTemplatePropertyService) {
        this.model = this.feedService.createFeedModel;

        var watchers = [];


        var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"]

        this.broadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, (event: any, index: any) => {
            if (index == parseInt(this.stepIndex)) {
                this.validate();
            } 
        });
        
        this.inputProcessorIdWatch = $scope.$watch(() => {
            return this.inputProcessorId;
        },(newVal: any, oldVal: any) => {
            if (newVal != null && this.initialInputProcessorId == null) {
                this.initialInputProcessorId = newVal;
            }
            this.updateInputProcessor(newVal);
            //mark the next step as not visited.. force the user to go to the next step
            this.stepperController.resetStep(parseInt(this.stepIndex) + 1);
            this.validate();
        });
        this.systemFeedNameWatch = $scope.$watch(() => {
            return this.model.systemFeedName;
        }, (newVal: any) => {
            this.validate();
        });
        this.templateIdWatch = $scope.$watch(() => {
            return this.model.templateId;
        }, (newVal: any) => {
            this.loading = true;
            this.getRegisteredTemplate();
        });
    }

    matchInputProcessor(inputProcessor: any, inputProcessors: any) {

        if (inputProcessor == null) {
            //input processor is null when feed is being created
            return undefined;
        }

        var matchingInput = _.find(inputProcessors, (input: any) => {
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
            this.registerTemplateService.setProcessorRenderTemplateUrl(this.model, 'create');
            this.inputProcessors = _.sortBy(this.model.inputProcessors, 'name')
            // Find controller services
            _.chain(this.inputProcessors.concat(this.model.nonInputProcessors))
                .pluck("properties")
                .flatten(true)
                .filter((property) => {
                    return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                })
                .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        } else {
            this.registerTemplateService.initializeProperties(template, 'create', this.model.properties);
            //sort them by name
            this.inputProcessors = _.sortBy(this.registerTemplateService.removeNonUserEditableProperties(template.inputProcessors, true), 'name')

            this.model.allowPreconditions = template.allowPreconditions;

            this.model.nonInputProcessors = this.registerTemplateService.removeNonUserEditableProperties(template.nonInputProcessors, false);
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
        if (this.inputProcessors.length === 0 && !_.some(this.model.nonInputProcessors, (processor: any) => {
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
            .filter((property) => {
                return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
            })
            .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        this.loading = false;
        this.model.isStream = template.isStream;
        this.validate();
    }

    /**
         * gets the Registered Template
         * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
         * @returns {HttpPromise}
         */
    getRegisteredTemplate() {
        if (this.model.templateId != null && this.model.templateId != '') {
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(this.model.templateId), { params: { feedEdit: true, allProperties: true } });
            promise.then( (response: any) => {
                this.initializeProperties(response.data);
            }, (err: any) =>{});
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

    clonedAndInputChanged(inputProcessorId:any) {
        return (this.model.cloned == true && this.inputChangedCounter > 1);
    }
    notCloned() {
        return (angular.isUndefined(this.model.cloned) || this.model.cloned == false);
    }
    /**
         * Updates the details when the processor is changed.
         *
         * @param {string} processorId the processor id
         */
    updateInputProcessor(processorId: any) {
        // Find the processor object
        var processor = _.find(this.inputProcessors, (processor: any) => {
            return (processor.processorId === processorId);
        });
        if (angular.isUndefined(processor)) {
            return;
        }

        this.inputChangedCounter++;

        

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

        } else if (this.model.templateTableOption != "DATA_TRANSFORMATION" && (this.clonedAndInputChanged(processorId) || this.notCloned())) {
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
        templateUrl: './define-feed-details.html',
    });