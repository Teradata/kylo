import * as angular from 'angular';
import * as _ from "underscore";
import {Component, Injector, Input, OnInit} from "@angular/core";
import { Templates } from "../../../../services/TemplateTypes";
import {DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
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
import {DynamicFormService} from "../../../../shared/dynamic-form/services/dynamic-form.service";
import {MatRadioChange} from "@angular/material";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {SectionHeader} from "../../../../shared/dynamic-form/model/SectionHeader";

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
        this.form = new FormGroup({});
    }

    getStepName() {
        return "Feed Details";
    }

    init(){
this.inputProcessors = [];
this.getRegisteredTemplate();


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

    updateInputProcessorFormElements(newInput:string,oldInput?:string,) {
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

    getRegisteredTemplate() :Observable<any>{
        if (this.feed.templateId != null && this.feed.templateId != '') {

            let params  = new HttpParams().append("feedEdit","true").append("allProperties","true");
            var promise = this.http.get(this.registerTemplatePropertyService.GET_REGISTERED_TEMPLATES_URL+"/"+this.feed.templateId, { params:params});
            promise.subscribe( (response: any) => {
                this.initializeProperties(response);

            }, (err: any) =>{});
            return promise;
        }
    }
    

    /**
     * Prepares the processor properties of the specified template for display.
     *
     * @param {Object} template the template with properties
     */
    initializeProperties(template: any) {
        if (angular.isDefined(this.feed.cloned) && this.feed.cloned == true) {
           this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(this.feed, 'create');
            this.inputProcessors = _.sortBy(this.feed.inputProcessors, 'name')
            // Find controller services
            _.chain(this.inputProcessors.concat(this.feed.nonInputProcessors))
                .pluck("properties")
                .flatten(true)
                .filter((property) => {
                    return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                })
                .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        } else {
           this.registerTemplatePropertyService.initializeProperties(template, 'create', this.feed.properties);
            //sort them by name
            this.inputProcessors = _.sortBy(this.registerTemplatePropertyService.removeNonUserEditableProperties(template.inputProcessors, true), 'name')

           // this.inputProcessors = template.inputProcessors;
            this.feed.allowPreconditions = template.allowPreconditions;

            this.feed.nonInputProcessors = this.registerTemplatePropertyService.removeNonUserEditableProperties(template.nonInputProcessors, false);
        }
        if (angular.isDefined(this.feed.inputProcessor)) {
            var match = this.matchInputProcessor(this.feed.inputProcessor, this.inputProcessors);
            if (angular.isDefined(match)) {
                this.inputProcessor = match;
                this.inputProcessorId = match.id;
            }
        }

        if (this.inputProcessorId == null && this.inputProcessors != null && this.inputProcessors.length > 0) {
            this.inputProcessorId = this.inputProcessors[0].id;
        }
        // Skip this step if it's empty
        if (this.inputProcessors.length === 0 && !_.some(this.feed.nonInputProcessors, (processor: any) => {
            return processor.userEditable
        }))

        // Find controller services
        _.chain(template.inputProcessors.concat(template.nonInputProcessors))
            .pluck("properties")
            .flatten(true)
            .filter((property) => {
                return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
            })
            .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        this.loading = false;
        this.feed.isStream = template.isStream;
  this.buildForm();

     //   this.validate();
    }

    matchInputProcessor(inputProcessor: Templates.Processor, inputProcessors: Templates.Processor[]) {

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

    private toFieldConfigOptions(property :Templates.Property):any {
        let key:string = property.idKey;
        let options = {key:key,label:property.propertyDescriptor.displayName,required:property.required,placeholder:property.propertyDescriptor.displayName, value:property.value,hint:property.propertyDescriptor.description};
        return options;

    }

   private buildForm(){
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

    toFieldConfig(processor:Templates.Processor):FieldConfig<any>[]{
        let elements :FieldConfig<any>[] = []
            let processorId = processor.id;


            processor.properties.filter((property:Templates.Property) => property.userEditable).map((property:Templates.Property) => {

                let fieldConfig:FieldConfig<any> = null;
                let fieldConfigOptions = this.toFieldConfigOptions(property);
                fieldConfigOptions.order= this.formFieldOrder;
                if(property.renderType == "text" || property.renderType == "email"){
                    let type = property.renderType;
                    if(property.sensitive) {
                        type = "password";
                    }
                    fieldConfigOptions.type = type;
                    fieldConfig = new InputText(fieldConfigOptions);
                }
                else if(property.renderType == "select"){
                    let options = (<any[]>property.propertyDescriptor.allowableValues).map(allowableValue => {
                        return {key: allowableValue.value, value: allowableValue.displayName}
                    });
                    fieldConfigOptions.options = options;
                    fieldConfig = new Select(fieldConfigOptions);
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