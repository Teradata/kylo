import * as angular from 'angular';
import * as _ from "underscore";
import {Component, Injector, Input, OnInit} from "@angular/core";
import { Templates } from "../../../../services/TemplateTypes";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {FormControl, FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FeedService} from "../../../../services/FeedService";
import {ITdDynamicElementConfig, TdDynamicElement} from "@covalent/dynamic-forms";
import {TdDynamicType} from "@covalent/dynamic-forms/services/dynamic-forms.service";
import {FieldConfig} from "../../../../shared/dynamic-form/model/FieldConfig";
import {InputText, InputType} from "../../../../shared/dynamic-form/model/InputText";
import {Select} from "../../../../shared/dynamic-form/model/Select";
import {Checkbox} from "../../../../shared/dynamic-form/model/Checkbox";
import {DynamicFormService} from "../../../../shared/dynamic-form/services/dynamic-form.service";
import {MatRadioChange} from "@angular/material";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import "rxjs/add/observable/from";
import 'rxjs/add/observable/forkJoin'
import {SectionHeader} from "../../../../shared/dynamic-form/model/SectionHeader";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {UiComponentsService} from "../../../../services/UiComponentsService";
import {RadioButton} from "../../../../shared/dynamic-form/model/RadioButton";
import {Textarea} from "../../../../shared/dynamic-form/model/Textarea";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {FieldGroup} from "../../../../shared/dynamic-form/model/FieldGroup";
import {DynamicFormBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-builder";
import {DynamicFormFieldGroupBuilder} from "../../../../shared/dynamic-form/services/dynamic-form-field-group-builder";
import {ConfigurationFieldBuilder, FieldConfigBuilder} from "../../../../shared/dynamic-form/services/field-config-builder";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";


export class FieldConfigurationState {

    formFieldOrder:number = 0
    /**
     * Map of the inputProcessorId to the controls related to that input
     * @type {{}}
     */
    inputFieldsMap :{[key: string]: FieldConfig<any>[]} ={};

    /**
     * Map of all processors other inputs to array of configs
     * @type {{}}
     */
    processorFieldMap:{[key: string]: FieldConfig<any>[]} ={};


    /**
     * Return all fieldconfig objects for a given input processor
     * @param {string} processorId
     * @return {FieldConfig<any>[]}
     */
    getFieldsForInput(processorId:string, emptyArrayIfNull:boolean = true){
        if(!this.hasInputFields(processorId) && emptyArrayIfNull){
            return [];
        }
        else {
            return this.inputFieldsMap[processorId];
        }
    }

    /**
     * are there input fields defined for the processor
     * @param {string} processorId
     * @return {boolean}
     */
    hasInputFields(processorId:string) {
        return this.inputFieldsMap[processorId] != undefined;
    }

    /**
     * are there fields defined for the processor
     * @param {string} processorId
     * @return {boolean}
     */
    hasNonInputFields(processorId:string) {
        return this.processorFieldMap[processorId] != undefined;
    }

    /**
     * Return all fieldconfig objects for a given input processor
     * @param {string} processorId
     * @return {FieldConfig<any>[]}
     */
    getFieldsForNonInput(processorId:string,emptyArrayIfNull:boolean = true){
        if(!this.hasNonInputFields(processorId) && emptyArrayIfNull){
            return [];
        }
        else {
            return this.processorFieldMap[processorId];
        }
    }

    getAndIncrementFieldOrder(){
        let order = this.formFieldOrder;
        this.formFieldOrder++;
        return order;
    }
    incrementAndGetFieldOrder(){
        this.formFieldOrder++;
        return this.formFieldOrder;
    }

    reset(){
        this.formFieldOrder = 0;
        this.inputFieldsMap = {}
        this.processorFieldMap = {}
    }
}

export class FieldConfigurationBuilder {
    formGroupBuilder:DynamicFormFieldGroupBuilder

    constructor(private state:FieldConfigurationState ) {

    }


    public  createFormFields(processors:Templates.Processor[]) :FieldConfig<any>[] {
        this.formGroupBuilder = new DynamicFormBuilder().column()
        let elements :FieldConfig<any>[] = []
        processors.forEach((processor :Templates.Processor) => {
            let processorConfig :FieldConfig<any>[] = this.toFieldConfig(processor);
            elements = elements.concat(processorConfig);
        });
        return elements;
    }

   private  buildField(property:Templates.Property):FieldConfig<any> {
        let field:FieldConfig<any>;
        //build the generic options to be used by all fields
       let label = property.propertyDescriptor.displayName || property.propertyDescriptor.name;
        let configBuilder = new ConfigurationFieldBuilder().setKey(property.nameKey).setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(label).setRequired(property.required).setValue(property.value).setModel(property).setHint(property.propertyDescriptor.description);

        if(this.isInputText(property)){
            //get the correct input type

            let type= property.renderType;
            if(property.sensitive) {
                type = "password";
            }
            let inputType:InputType = <InputType>InputType[type] || InputType.text;

            //create the field
            field = this.formGroupBuilder.text().update(configBuilder).setType(inputType).build();

        }
        else if(property.renderType == "select"){
            field = this.formGroupBuilder.select().update(configBuilder).setOptions(this.getSelectOptions(property)).build()

        }
        else if(property.renderType == "radio") {
            field = this.formGroupBuilder.radio().update(configBuilder).setOptions(this.getSelectOptions(property)).build();

        }
        else if(property.renderType == "checkbox-true-false" || property.renderType == "checkbox-custom") {
            let trueValue = property.renderOptions['trueValue'] || 'true';
            let falseValue = property.renderOptions['falseValue'] || 'false';
            field = this.formGroupBuilder.checkbox().update(configBuilder).setTrueValue(trueValue).setFalseValue(falseValue).build();
        }
        else if(property.renderType == "textarea") {
            field = this.formGroupBuilder.textarea().update(configBuilder).build();
        }
        return field;
    }

    /**
     * convert the property allowable values to label,value objects
     * @param {Templates.Property} property
     * @return {{label: string; value: string}[]}
     */
    private getSelectOptions(property:Templates.Property):({label:string,value:string})[]{
        //get the select options
        let options:({label:string,value:string})[] = [];

        if(property.propertyDescriptor.allowableValues && property.propertyDescriptor.allowableValues.length >0) {
            options = (<any[]>property.propertyDescriptor.allowableValues).map(allowableValue => {
                return {label: allowableValue.displayName,value: allowableValue.value}
            });
        }
        else if(property.renderOptions && property.renderOptions.selectOptions && property.renderOptions.selectOptions.length >0) {
            let selectOptions = JSON.parse(property.renderOptions.selectOptions)
            options = (<any[]>selectOptions).map(allowableValue => {
                return {label: allowableValue,value: allowableValue}
            });
        }
        //add in the not set value
        if(!property.required){
            options.unshift({label:"Not Set",value:""});
        }
        return options;
    }



    private toFieldConfig(processor:Templates.Processor):FieldConfig<any>[]{
        let elements :FieldConfig<any>[] = []
        let processorId = processor.id;


        processor.properties.filter((property:Templates.Property) => property.userEditable).map((property:Templates.Property) => {

            let fieldConfig:FieldConfig<any> = this.buildField(property);

            if(property.inputProperty){
                if(this.state.inputFieldsMap[processor.id] == undefined){
                    this.state.inputFieldsMap[processor.id] = [];
                }
                this.state.inputFieldsMap[processor.id].push(fieldConfig);
            }
            else {
                if(this.state.processorFieldMap[processor.id] == undefined){
                    this.state.processorFieldMap[processor.id] = [];
                    //add a new SectionHeader
                    let sectionHeader =this.formGroupBuilder.sectionHeader().setOrder(this.state.getAndIncrementFieldOrder()).setPlaceholder(processor.name).setShowDivider(true).build();
                    elements.push(sectionHeader);
                    this.state.processorFieldMap[processor.id].push(sectionHeader);
                }
                this.state.processorFieldMap[processor.id].push(fieldConfig);
            }

            elements.push(fieldConfig);
        });
        return elements;
    }

    private isInputText(property:Templates.Property){
        return (property.renderType == null || property.renderType == "text" || property.renderType == "email" || property.renderType == "number" || property.renderType == "password");
    }

}




@Component({
    selector: "define-feed-step-feed-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.html"
})
export class DefineFeedStepFeedDetailsComponent extends AbstractFeedStepComponent {


    /**
     * processors modified for display
     */
    inputProcessors:Templates.Processor[];

    /**
     * selected input processor id
     */
    inputProcessorId:string;

    oldInputProcessorId:string;

    /**
     * selected input processor
     */
    inputProcessor:Templates.Processor;


    inputProcessorFormElements: ITdDynamicElementConfig[];

    additionalProcessorFormElements :ITdDynamicElementConfig[];

    private loading: boolean;

    private feedService :FeedService;

    public form :FormGroup;

    /**
     * The array of groups that will be passed to the dynamic form component
     * @type {any[]}
     */
    public inputProcessorFieldGroups:FieldGroup[] = []

    /**
     * The array of groups that will be passed to the dynamic form component
     * @type {any[]}
     */
    public nonInputProcessorFieldGroups:FieldGroup[] = []

    private fieldConfigurationState :FieldConfigurationState = new FieldConfigurationState();

    private uiComponentsService :UiComponentsService;



    private registerTemplatePropertyService :RegisterTemplatePropertyService;

    constructor(  defineFeedService:DefineFeedService,  stateService:StateService, private http:HttpClient,
                  private $$angularInjector: Injector,
                  private dynamicFormService:DynamicFormService, feedLoadingService:FeedLoadingService,
                  dialogService: TdDialogService, feedSideNavService:FeedSideNavService) {
    super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.feedService = $$angularInjector.get("FeedService");
        this.registerTemplatePropertyService = this.$$angularInjector.get("RegisterTemplatePropertyService");
        this.uiComponentsService = $$angularInjector.get("UiComponentsService");
        this.form = new FormGroup({});

        //add a single group to each input and non input array
        this.inputProcessorFieldGroups.push(new FieldGroup())
        this.nonInputProcessorFieldGroups.push(new FieldGroup())
    }

    getStepName() {
        return FeedStepConstants.STEP_FEED_DETAILS;
    }

    init(){
        this.inputProcessors = [];
        if(this.feed.isNew()) {
                this.initializeTemplateProperties();
        }
        else {
            this.mergeTemplateDataWithFeed(this.feed);
        }

        //listen when the form is valid or invalid
        this.subscribeToFormChanges(this.form);

    }

    /**
     * called before saving
     */
    protected applyUpdatesToFeed(){
        let properties :Templates.Property[] = [];
        if (this.inputProcessor != null) {
            this.inputProcessor.properties.forEach(property => {
                //this.FeedPropertyService.initSensitivePropertyForSaving(property)
                properties.push(property);
            });
        }

        this.feed.nonInputProcessors.forEach(processor => {
            processor.properties.forEach(property =>   {
                //this.FeedPropertyService.initSensitivePropertyForSaving(property)
                properties.push(property);})
        });

        if(this.inputProcessor) {
            this.feed.inputProcessorName = this.inputProcessor.name;
        }
        this.feed.properties = properties;

    }

    onInputProcessorChange(event:MatRadioChange){
        // Find the processor object
        let oldId = this.oldInputProcessorId;

        let processors = this.inputProcessors.filter((processor: Templates.Processor) => processor.id === event.value);
        if(processors.length ==0){
            return;
        }
        let processor:Templates.Processor = processors[0];
        this.inputProcessor = processor;
        this.feed.inputProcessor = processor;
        this.feed.inputProcessorType = processor.type;
        this.updateInputProcessorFormElements(event.value,oldId);

        //set the old value
        this.oldInputProcessorId = event.value;
        this.step.markDirty();
    }

    private getInputProcessorFieldGroup():FieldGroup {
        return this.inputProcessorFieldGroups[0];
    }

    private getNonInputProcessorFieldGroup():FieldGroup {
        return this.nonInputProcessorFieldGroups[0];
    }


    private updateInputProcessorFormElements(newInput:string,oldInput?:string) {
        let oldConfig :FieldConfig<any>[];
        if(oldInput) {
            oldConfig = this.fieldConfigurationState.getFieldsForInput(oldInput);
            if(oldConfig.length >0) {
                oldConfig.forEach(config => {
                    let control = this.form.controls[config.key];
                    if(control != undefined) {
                        this.form.removeControl(config.key);
                    }
                });
            }
        }

        let inputFields = this.getInputProcessorFieldGroup().fields = this.fieldConfigurationState.getFieldsForInput(newInput);
        this.dynamicFormService.addToFormGroup(inputFields, this.form);

    }

    initializeTemplateProperties() {
        if (!this.feed.propertiesInitialized && this.feed.templateId != null && this.feed.templateId != '') {

            let params  = new HttpParams().append("feedEdit","true").append("allProperties","true");
            var promise = this.http.get(this.registerTemplatePropertyService.GET_REGISTERED_TEMPLATES_URL+"/"+this.feed.templateId, { params:params});
            promise.subscribe( (template: any) => {


                if (angular.isDefined(this.feed.cloned) && this.feed.cloned == true) {
                    this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(this.feed, 'create');
                    this.defineFeedService.sortAndSetupFeedProperties(this.feed);

                } else {
                    this.defineFeedService.setupFeedProperties(this.feed,template, 'create')
                    this.inputProcessor = this.feed.inputProcessor;
                    this.inputProcessors = this.feed.inputProcessors;
                    this.buildForm();
                    this.feed.propertiesInitialized = true;

                    this.subscribeToFormDirtyCheck(this.form);
                }

            }, (err: any) =>{});
            return promise;
        }
        else if(this.feed.propertiesInitialized){
            this.inputProcessor = this.feed.inputProcessor;
            this.inputProcessors = this.feed.inputProcessors;
            this.buildForm();
            this.subscribeToFormDirtyCheck(this.form);

        }
    }

    public mergeTemplateDataWithFeed(feed:Feed){

        if (!feed.propertiesInitialized) {
            let observables: Observable<any>[] = [];
            let steps = feed.steps;
            let feedCopy = feed.copy(false);
            delete feedCopy.steps;
            observables[0] = this.http.post(RestUrlConstants.MERGE_FEED_WITH_TEMPLATE(feed.id), feedCopy, {headers: {'Content-Type': 'application/json; charset=UTF-8'}}),
            observables[1] = Observable.from(this.uiComponentsService.getProcessorTemplates());

                Observable.forkJoin(observables[0], observables[1]).subscribe(results => {
                    const updatedFeedResponse :Feed = results[0];
                    const templateResponse:any = results[1];
                    //const [updatedFeedResponse , templateResponse] = results;
                    if (updatedFeedResponse == undefined) {
                        //ERROR out
                        //@TODO present error or return observable.error()
                    }
                    else {
                        //merge the properties back into this feed
                        feed.properties = updatedFeedResponse.properties;
                        feed.inputProcessors = updatedFeedResponse.inputProcessors;
                        feed.nonInputProcessors = updatedFeedResponse.nonInputProcessors;
                        feed.registeredTemplate = updatedFeedResponse.registeredTemplate;
                        this.defineFeedService.setupFeedProperties(feed,feed.registeredTemplate, 'edit');
                        feed.propertiesInitialized = true;
                        this.inputProcessor = feed.inputProcessor;
                        this.inputProcessors = feed.inputProcessors;
                        this.buildForm();
                        this.subscribeToFormDirtyCheck(this.form);

                        //@TODO add in  access control

                        /*
                          var entityAccessControlled = accessControlService.isEntityAccessControlled();
                            //Apply the entity access permissions
                            var requests = {
                                entityEditAccess: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, self.model),
                                entityExportAccess: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, self.model),
                                entityStartAccess: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.START, self.model),
                                entityPermissionAccess: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS, self.model),
                                functionalAccess: accessControlService.getUserAllowedActions()
                            };
                            $q.all(requests).then(function (response:any) {
                                var allowEditAccess =  accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions);
                                var allowAdminAccess =  accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, response.functionalAccess.actions);
                                var slaAccess =  accessControlService.hasAction(AccessControlService.SLA_ACCESS, response.functionalAccess.actions);
                                var allowExport = accessControlService.hasAction(AccessControlService.FEEDS_EXPORT, response.functionalAccess.actions);
                                var allowStart = accessControlService.hasAction(AccessControlService.FEEDS_EDIT, response.functionalAccess.actions);

                                self.allowEdit = response.entityEditAccess && allowEditAccess;
                                self.allowChangePermissions = entityAccessControlled && response.entityPermissionAccess && allowEditAccess;
                                self.allowAdmin = allowAdminAccess;
                                self.allowSlaAccess = slaAccess;
                                self.allowExport = response.entityExportAccess && allowExport;
                                self.allowStart = response.entityStartAccess && allowStart;
                            });
                         */

                    }


                })
            }
        else {
          //  this.defineFeedService.setupFeedProperties(this.feed,this.feed.registeredTemplate, 'edit')
            this.inputProcessor = feed.inputProcessor;
            this.inputProcessors = feed.inputProcessors;
            this.buildForm();
            this.subscribeToFormDirtyCheck(this.form);
            }
    }

    private buildForm(){
        this.fieldConfigurationState.reset();

        //ensure the input processorId is set
        if(this.inputProcessorId == undefined && this.inputProcessor != undefined){
            this.inputProcessorId = this.inputProcessor.id;
        }

        new FieldConfigurationBuilder(this.fieldConfigurationState).createFormFields(this.inputProcessors).sort((n1,n2) => {
            return n1.order - n2.order;
        });

        let nonInputProcessorFields =   new FieldConfigurationBuilder(this.fieldConfigurationState).createFormFields(this.feed.nonInputProcessors).sort((n1,n2) => {
            return n1.order - n2.order;
        });



        //populate the form with the correct input processors
        let inputProcessorFields = this.fieldConfigurationState.getFieldsForInput(this.inputProcessorId);
        this.getInputProcessorFieldGroup().fields = inputProcessorFields;
        this.dynamicFormService.addToFormGroup(inputProcessorFields, this.form);

        //add all the other form fields
        this.getNonInputProcessorFieldGroup().fields = nonInputProcessorFields;
        this.dynamicFormService.addToFormGroup(nonInputProcessorFields, this.form);
    }





}