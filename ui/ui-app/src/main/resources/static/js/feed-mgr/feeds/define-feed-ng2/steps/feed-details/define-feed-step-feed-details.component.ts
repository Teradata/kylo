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
import {InputText} from "../../../../shared/dynamic-form/model/InputText";
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

    public inputProcessorFields :FieldConfig<any>[] = [];


    public nonInputProcessorFields :FieldConfig<any>[] = [];
    /**
     * Map of the inputProcessorId to the controls related to that input
     * @type {{}}
     */
    private inputFieldsMap :{[key: string]: FieldConfig<any>[]} ={};

    private formFieldOrder:number = 0;

    private uiComponentsService :UiComponentsService;


    /**
     * Map of all processors other inputs to array of configs
     * @type {{}}
     */
    processorFieldMap:{[key: string]: FieldConfig<any>[]} ={};

    private registerTemplatePropertyService :RegisterTemplatePropertyService;

    constructor(  defineFeedService:DefineFeedService,  stateService:StateService, private http:HttpClient,private $$angularInjector: Injector,private dynamicFormService:DynamicFormService) {
        super(defineFeedService,stateService);
        this.feedService = $$angularInjector.get("FeedService");
        this.registerTemplatePropertyService = this.$$angularInjector.get("RegisterTemplatePropertyService");
        this.uiComponentsService = $$angularInjector.get("UiComponentsService");
        this.form = new FormGroup({});
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


    }

    /**
     * called before saving
     */
    updateFeedService(){
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

        this.defineFeedService.setFeed(this.feed);
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
    }


    private updateInputProcessorFormElements(newInput:string,oldInput?:string) {
        if(oldInput) {
            let oldConfig = this.inputFieldsMap[oldInput];
            if(oldConfig != undefined) {
                oldConfig.forEach(config => {
                    let control = this.form.controls[config.key];
                    if(control != undefined) {
                        this.form.removeControl(config.key);
                    }
                });
            }
        }

        this.inputProcessorFields = this.inputFieldsMap[newInput];
        if(this.inputProcessorFields != undefined) {
            this.dynamicFormService.addToFormGroup(this.inputProcessorFields, this.form);
        }
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

                    //   this.validate();
                }




            }, (err: any) =>{});
            return promise;
        }
        else if(this.feed.propertiesInitialized){
            this.inputProcessor = this.feed.inputProcessor;
            this.inputProcessors = this.feed.inputProcessors;
            this.buildForm();

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
            }
    }



    private toFieldConfigOptions(property :Templates.Property):any {
        let key:string = property.idKey;
        let options = {key:key,label:property.propertyDescriptor.displayName,required:property.required,placeholder:property.propertyDescriptor.displayName, value:property.value,hint:property.propertyDescriptor.description};
        return options;

    }

   private buildForm(){
        if(this.inputProcessorId == undefined && this.inputProcessor != undefined){
            this.inputProcessorId = this.inputProcessor.id;
        }
        this.formFieldOrder = 0

        this.createFormFields(this.inputProcessors).sort((n1,n2) => {
            return n1.order - n2.order;
        });
         this.nonInputProcessorFields =  this.createFormFields(this.feed.nonInputProcessors).sort((n1,n2) => {
            return n1.order - n2.order;
        });

        this.inputProcessorFields = this.inputFieldsMap[this.inputProcessorId];
        if(this.inputProcessorFields == undefined){
            this.inputProcessorFields = [];
        }
        this.dynamicFormService.addToFormGroup(this.inputProcessorFields, this.form);
        this.dynamicFormService.addToFormGroup(this.nonInputProcessorFields, this.form);
    }

  private  createFormFields(processors:Templates.Processor[]) :FieldConfig<any>[] {

        let elements :FieldConfig<any>[] = []
        processors.forEach((processor :Templates.Processor) => {
            let processorConfig :FieldConfig<any>[] = this.toFieldConfig(processor);
            elements = elements.concat(processorConfig);
           });
       return elements;
    }
    private isInputText(property:Templates.Property){
        return (property.renderType == null || property.renderType == "text" || property.renderType == "email" || property.renderType == "number" || property.renderType == "password");
    }

    toFieldConfig(processor:Templates.Processor):FieldConfig<any>[]{
        let elements :FieldConfig<any>[] = []
            let processorId = processor.id;


            processor.properties.filter((property:Templates.Property) => property.userEditable).map((property:Templates.Property) => {

                let fieldConfig:FieldConfig<any> = null;
                let fieldConfigOptions = this.toFieldConfigOptions(property);
                fieldConfigOptions.order= this.formFieldOrder;

                if(this.isInputText(property)){
                    let type = property.renderType;
                    if(property.sensitive) {
                        type = "password";
                    }
                    fieldConfigOptions.type = type;
                    fieldConfig = new InputText(fieldConfigOptions);
                }
                else if(property.renderType == "select" || property.renderType == "radio"){

                    let options :any[] = [];
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

                    fieldConfigOptions.options = options;
                    if(property.renderType == "select") {
                        fieldConfig = new Select(fieldConfigOptions);
                    }
                    else if(property.renderType == "radio") {
                        fieldConfig = new RadioButton(fieldConfigOptions);
                    }
                }
                else if(property.renderType == "checkbox-true-false" || property.renderType == "checkbox-custom") {
                    //default value is true, false.  Only need to set it if its custom
                    if (property.renderType == "checkbox-custom") {
                        fieldConfigOptions.trueValue = property.renderOptions['trueValue'];
                        fieldConfigOptions.falseValue = property.renderOptions['falseValue'];
                    }
                    fieldConfig = new Checkbox(fieldConfigOptions);

                }
                else if(property.renderType == "textarea") {
                    fieldConfig = new Textarea(fieldConfigOptions);
                }
                fieldConfig.model = property;


                if(property.inputProperty){
                    if(this.inputFieldsMap[processor.id] == undefined){
                        this.inputFieldsMap[processor.id] = [];
                    }
                    this.inputFieldsMap[processor.id].push(fieldConfig);
                }
                else {
                    if(this.processorFieldMap[processor.id] == undefined){
                        this.processorFieldMap[processor.id] = [];
                        //add a new SectionHeader
                        let sectionHeader =new SectionHeader({order:this.formFieldOrder,label:processor.name});
                        elements.push(sectionHeader);
                        this.processorFieldMap[processor.id].push(sectionHeader);
                        //increment the order again and set it to fieldConfig
                        this.formFieldOrder +=1;
                        fieldConfig.order = this.formFieldOrder;
                    }
                    this.processorFieldMap[processor.id].push(fieldConfig);

                }

                elements.push(fieldConfig);
               this.formFieldOrder +=1;
            });
            return elements;
    }

}