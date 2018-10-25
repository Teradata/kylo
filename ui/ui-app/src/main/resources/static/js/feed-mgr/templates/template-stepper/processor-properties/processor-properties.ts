import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "../../module-name";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import { UiComponentsService } from '../../../services/UiComponentsService';
import { FeedService } from '../../../services/FeedService';
import {RegisterTemplatePropertyService} from "../../../services/RegisterTemplatePropertyService";

export class RegisterProcessorPropertiesController {

    model: any;
    isValid: boolean = true;
    /**
         * List of available expression properties.
         * @type {Object[]}
         */
    availableExpressionProperties: any;
    /**
         * Expression properties for auto-complete.
         * @type {Object[]}
         */
    expressionProperties: any;
    stepNumber: any;
    stepIndex: any;
    propertiesThatNeedAttention: boolean = false;
    showOnlySelected: boolean = false;
    filterString: any = null;
    propertyRenderTypes: any[] = [];
    processors: any[] = [];
    visiblePropertyCount: number = 0;
    allProperties: any[] = [];
    processorPropertiesFieldName: any;
    selectedProperties: any;
    topIndex: any;
    propertiesForm: any;

    ngOnInit() {
        //Filter attrs
        this.stepNumber = parseInt(this.stepIndex) + 1
    }
    $onInit() {
        this.ngOnInit();
    }

    static readonly $inject = ["$scope", "$element", "$http", "$q", "$mdToast",
        "$location", "$window", "RestUrlService", "RegisterTemplateService",
        "FeedService", "UiComponentsService","RegisterTemplatePropertyService"];

    constructor(private $scope: IScope, private $element: any, private $http: angular.IHttpService, private $q: angular.IQService, private $mdToast: angular.material.IToastService
        , private $location: angular.ILocationService, private $window: angular.IWindowService, private RestUrlService: any, private registerTemplateService: RegisterTemplateServiceFactory
        , private feedService: FeedService, private uiComponentsService: UiComponentsService,
                private registerTemplatePropertyService :RegisterTemplatePropertyService) {

        this.model = registerTemplateService.model;


        this.availableExpressionProperties = registerTemplatePropertyService.propertyList;


        this.expressionProperties = this.availableExpressionProperties;

        $scope.$watch(() => {
            return this.registerTemplatePropertyService.codemirrorTypes;
        }, (newVal: any) => {
            this.initializeRenderTypes();
        })

        $scope.$watchCollection(() => {
            return this.model[this.processorPropertiesFieldName]
        }, () => {
            this.transformPropertiesToArray();
            //  this.processors = filterProcessors(this.propertiesThatNeedAttention, this.showOnlySelected);
            //  countVisibleProperties();
        })
        // Update expression properties when table option changes
        $scope.$watch(() => {
            return this.model.templateTableOption;
        }, () => {
            if (this.model.templateTableOption !== "NO_TABLE" && angular.isArray(this.registerTemplatePropertyService.propertyList)) {
                this.uiComponentsService.getTemplateTableOptionMetadataProperties(this.model.templateTableOption)
                    .then((tableOptionMetadataProperties: any) => {
                        this.availableExpressionProperties = this.registerTemplatePropertyService.propertyList.concat(tableOptionMetadataProperties);
                    });
            } else {
                this.availableExpressionProperties = this.registerTemplatePropertyService.propertyList;
            }
        });
    };

    // $anchorScroll.yOffset = 200;
    searchExpressionProperties = (term: any) => {
        this.expressionProperties = this.availableExpressionProperties.filter((property: any) => {
            return (property.key.toUpperCase().indexOf(term.toUpperCase()) >= 0);
        });
        return this.$q.when(this.expressionProperties);
    };

    getExpressionPropertyTextRaw = (item: any) => {
        return '${' + item.key + '}';
    };

    inputProcessorSelectionInvalid = () => {
        var selectedList = _.filter(this.model.inputProcessors, (processor: any) => {
            return processor.selected;
        });
        if (selectedList == null || selectedList.length == 0) {
            this.isValid = false;
            return true;
        }
        else {
            this.isValid = true;
            return false;
        }

    }

    minProcessorItems = () => {
        var windowHeight = angular.element(this.$window).height();
        var newHeight = windowHeight - 450;
        var processorHeight = 48;
        var minItems = Math.round(newHeight / processorHeight);
        return minItems;
    }


    scrollToProcessor = (processor: any) => {
        var topIndex = processor.topIndex;
        this.topIndex = topIndex;
    }
    transformPropertiesToArray = () => {
        var propertiesKey = this.processorPropertiesFieldName + "Properties";
        var processorsKey = this.processorPropertiesFieldName + "Processors";
        this.allProperties = _.filter(this.model[propertiesKey], (prop: any) => { return prop.hidden == undefined || prop.hidden == false });

        // Find controller services
        _.chain(this.allProperties).filter((property: any) => {
            return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
        })
            .each(this.feedService.findControllerServicesForProperty);


        this.processors = this.model[processorsKey];
        if (this.showOnlySelected) {
            this.showSelected();
        }
    }

    showSelected = () => {
        var selectedProperties: any = [];
        angular.forEach(this.allProperties, (property: any) => {
            if (property.selected) {
                selectedProperties.push(property);
            }
        });

        //sort them by processor name and property key
        var propertiesAndProcessors = this.registerTemplateService.sortPropertiesForDisplay(selectedProperties);
        this.allProperties = propertiesAndProcessors.properties;
        this.processors = propertiesAndProcessors.processors;
    }

    onShowOnlySelected = () => {
        this.transformPropertiesToArray();
    }

    changedPropertyInput = (property: any) => {
        property.changed = true;
    }

    keydownPropertyInput = (property: any) => {
        if (!property.changed && property.sensitive) {
            property.value = "";
        }
    }

    onRenderTypeChange = (property: any) => {
        if (property.renderType == 'select' && (property.propertyDescriptor.allowableValues == undefined || property.propertyDescriptor.allowableValues == null || property.propertyDescriptor.allowableValues.length == 0)) {
            if (property.selectOptions == undefined) {
                property.selectOptions = [];
            }
            property.renderOptions['selectCustom'] = 'true';
        }
        else {
            property.renderOptions['selectCustom'] = 'false';
            property.selectOptions = undefined;
            if (property.renderType == 'password') {
                property.sensitive = true;
            }
        }
    }

        toggleSetAsEmptyString(property:any){
            if(property.value == ''){
                property.value = null;
            }
            else {
                property.value = '';
            }
        }

    customSelectOptionChanged = (property: any) => {
        var str = JSON.stringify(property.selectOptions);
        property.renderOptions['selectOptions'] = str;

    }


    initializeRenderTypes = () => {
        angular.forEach(this.registerTemplatePropertyService.codemirrorTypes, (label: any, type: any) => {
            this.propertyRenderTypes.push({ type: type, label: label, codemirror: true });
        });
    }


    /**
     * Is property registration change info available from Kylo services?
     * @param property
     * @returns {boolean}
     */
    registrationChangeInfoAvailable(property: any): boolean {
        return ((property.registrationChangeInfo != null)
            && (
            (property.registrationChangeInfo.valueFromNewerNiFiTemplate != null)
            || (property.registrationChangeInfo.valueFromOlderNiFiTemplate != null)
            || (property.registrationChangeInfo.valueRegisteredInKyloTemplateFromOlderNiFiTemplate != null)));
    }

    /**
     * Should option to accept property value from NiFi template be presented?
     * @param property
     * @returns {boolean}
     */
    allowUserToAcceptPropertyValueFromNiFi(property: any): boolean {
        return ((this.registrationChangeInfoAvailable(property))
            && (property.registrationChangeInfo.valueFromNewerNiFiTemplate != property.registrationChangeInfo.valueFromOlderNiFiTemplate)
        && (property.registrationChangeInfo.valueFromOlderNiFiTemplate != property.registrationChangeInfo.valueRegisteredInKyloTemplateFromOlderNiFiTemplate)
        && (property.value != property.registrationChangeInfo.valueFromNewerNiFiTemplate));
    }

    /**
     * Update property value to match value defined in NiFi template
     * @param property
     */
    acceptPropertyValueFromNiFi(property: any): void {
        property.value = property.registrationChangeInfo.valueFromNewerNiFiTemplate;
    }
}
angular.module(moduleName)
    .component('thinkbigRegisterProcessorProperties', {
        bindings: {
            stepIndex: '@',
            cardTitle: '@',
            processorPropertiesFieldName: '@'
        },
        controllerAs: 'vm',
        templateUrl: './processor-properties.html',
        controller: RegisterProcessorPropertiesController,
    });
