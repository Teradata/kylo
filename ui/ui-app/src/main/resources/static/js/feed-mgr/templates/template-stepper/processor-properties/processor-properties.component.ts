import * as angular from 'angular';
import * as _ from "underscore";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import { UiComponentsService } from '../../../services/UiComponentsService';
import { FeedService } from '../../../services/FeedService';
import { Component, Input, Inject, OnInit, SimpleChanges } from '@angular/core';
import { RestUrlService } from '../../../services/RestUrlService';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { RegisterTemplatePropertyService } from '../../../services/RegisterTemplatePropertyService';

@Component({
    selector: 'thinkbig-register-processor-properties',
    templateUrl:'js/feed-mgr/templates/template-stepper/processor-properties/processor-properties.html'
})
export class RegisterProcessorPropertiesController implements OnInit {

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
    stepNumber: number;
    propertiesThatNeedAttention: boolean = false;
    showOnlySelected: boolean = false;
    filterString: string = null;
    propertyRenderTypes: any[] = [];
    processors: any[] = [];
    visiblePropertyCount: number = 0;
    allProperties: any[] = [];
    selectedProperties: any;
    topIndex: number;

    @Input() cardTitle: string;
    @Input() stepIndex: string;
    @Input() processorPropertiesFieldName: string;

    @Input() formGroup: FormGroup;

    private createFormControls(property:any) {
        if(property.propertyDescriptor.allowableValues == null && property.selected == true 
            && property.required && !property.userEditable) {
            this.formGroup.addControl(property.key,new FormControl(null,Validators.required));
        }
    }


    ngOnInit() {
        
        //Filter attrs
        this.stepNumber = parseInt(this.stepIndex) + 1;

        this.model = this.registerTemplateService.model;

        this.availableExpressionProperties = this.registerTemplatePropertyService.propertyList;

        this.expressionProperties = this.availableExpressionProperties;

        this.registerTemplatePropertyService.codeMirrorTypesObserver.subscribe((codemirrorTypes)=>{
            this.registerTemplatePropertyService.codemirrorTypes = codemirrorTypes;
            this.initializeRenderTypes();
        })

        if (this.allProperties.length == 0 && this.model[this.processorPropertiesFieldName + "Properties"]) {
            this.transformPropertiesToArray();
        }

        this.registerTemplateService.modelInputObserver.subscribe(()=>{
            this.transformPropertiesToArray();
        })

        this.registerTemplateService.modelTemplateTableOptionObserver.subscribe((model)=>{
            if (this.model.templateTableOption !== "NO_TABLE" && angular.isArray(this.registerTemplatePropertyService.propertyList)) {
                this.uiComponentsService.getTemplateTableOptionMetadataProperties(this.model.templateTableOption)
                    .then((tableOptionMetadataProperties: any) => {
                        this.availableExpressionProperties = this.registerTemplatePropertyService.propertyList.concat(tableOptionMetadataProperties);
                    });
            } else {
                this.availableExpressionProperties = this.registerTemplatePropertyService.propertyList;
            }   
        })
    }
    
    constructor(private RestUrlService: RestUrlService, 
                private registerTemplateService: RegisterTemplateServiceFactory,
                private registerTemplatePropertyService: RegisterTemplatePropertyService,
                private feedService: FeedService, 
                private uiComponentsService: UiComponentsService) {}

    searchExpressionProperties = (term: any) => {
         return this.expressionProperties = this.availableExpressionProperties.filter((property: any) => {
            return (property.key.toUpperCase().indexOf(term.toUpperCase()) >= 0);
        });
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
        var windowHeight = angular.element(window).height();
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

        _.each(this.allProperties,(property) => this.createFormControls(property));

        this.processors = this.model[processorsKey];
        if (this.showOnlySelected) {
            this.showSelected();
        }
        
        // Find controller services
        _.chain(this.allProperties).filter((property: any) => {
            return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
        }).each(this.feedService.findControllerServicesForProperty);

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

}
