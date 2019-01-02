import * as _ from "underscore";
import * as $ from "jquery";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import { UiComponentsService } from '../../../services/UiComponentsService';
import { FeedService } from '../../../services/FeedService';
import { Component, Input, Inject, OnInit, SimpleChanges, ViewEncapsulation } from '@angular/core';
import { RestUrlService } from '../../../services/RestUrlService';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { RegisterTemplatePropertyService } from '../../../services/RegisterTemplatePropertyService';
import { TdDataTableService, TdDataTableSortingOrder } from '@covalent/core/data-table';
import { ObjectUtils } from '../../../../../lib/common/utils/object-utils';

@Component({
    selector: 'thinkbig-register-processor-properties',
    templateUrl:'./processor-properties.html',
    encapsulation: ViewEncapsulation.None,
    styles: [
        `
            mat-hint {
                font-size: 12px;
            }
            thinkbig-register-processor-properties .mat-select-value {
                min-width: 64px !important;
                width: auto !important;
            }
        `
    ]
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
            if (this.model.templateTableOption !== "NO_TABLE" && Array.isArray(this.registerTemplatePropertyService.propertyList)) {
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
                private uiComponentsService: UiComponentsService,
                private _dataTableService: TdDataTableService) {}

    searchExpressionProperties (term: any) {
         return this.expressionProperties = this.availableExpressionProperties.filter((property: any) => {
            return (property.key.toUpperCase().indexOf(term.toUpperCase()) >= 0);
        });
    };

    getExpressionPropertyTextRaw (item: any) {
        return '${' + item.key + '}';
    };

    inputProcessorSelectionInvalid () {
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

    minProcessorItems () {
        var windowHeight = $(window).height();
        var newHeight = windowHeight - 450;
        var processorHeight = 48;
        var minItems = Math.round(newHeight / processorHeight);
        return minItems;
    }

    scrollToProcessor (processor: any) {
        var topIndex = processor.topIndex;
        this.topIndex = topIndex;
    }

    transformPropertiesToArray () {
        var propertiesKey = this.processorPropertiesFieldName + "Properties";
        var processorsKey = this.processorPropertiesFieldName + "Processors";
        this.allProperties = _.filter(this.model[propertiesKey], (prop: any) => { return prop.hidden == undefined || prop.hidden == false });

        _.each(this.allProperties,(property) => this.createFormControls(property));

        this.processors = this.model[processorsKey];
        if (this.showOnlySelected) {
            this.showSelected();
        }
        _.each(this.allProperties, (property) => {
            if(property.propertyDescriptor.allowableValues){
                this._dataTableService.sortData(property.propertyDescriptor.allowableValues,
                    "value",TdDataTableSortingOrder.Ascending);
            }
            if(property.renderTypes !== null){
                this._dataTableService.sortData(property.renderTypes,
                    "label",TdDataTableSortingOrder.Ascending);
            }
        });
        // Find controller services
        _.chain(this.allProperties).filter((property: any) => {
            return _.isObject(property.propertyDescriptor) && ObjectUtils.isString(property.propertyDescriptor.identifiesControllerService);
        }).each((property : any) => {this.feedService.findControllerServicesForProperty(property)});

    }

    showSelected () {
        var selectedProperties: any = [];
        _.forEach(this.allProperties, (property: any) => {
            if (property.selected) {
                selectedProperties.push(property);
            }
        });

        //sort them by processor name and property key
        var propertiesAndProcessors = this.registerTemplateService.sortPropertiesForDisplay(selectedProperties);
        this.allProperties = propertiesAndProcessors.properties;
        this.processors = propertiesAndProcessors.processors;
    }

    onShowOnlySelected () {
        this.transformPropertiesToArray();
    }

    changedPropertyInput (property: any) {
        property.changed = true;
    }

    keydownPropertyInput (property: any) {
        if (!property.changed && property.sensitive) {
            property.value = "";
        }
    }

    onRenderTypeChange (property: any) {
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

    customSelectOptionChanged (property: any) {
        var str = JSON.stringify(property.selectOptions);
        property.renderOptions['selectOptions'] = str;

    }

    initializeRenderTypes () {
        _.each(this.registerTemplatePropertyService.codemirrorTypes, (label: any, type: any) => {
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
