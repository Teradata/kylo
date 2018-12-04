import {HttpClient, HttpParams} from "@angular/common/http";
import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormArray, FormControl, FormGroup,Validators} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {StateService} from "@uirouter/angular";
import {Subscription} from "rxjs/Subscription";
import * as _ from "underscore";

import {ProcessorRef} from "../../../../../../lib/feed/processor/processor-ref";
import {Step} from "../../../../model/feed/feed-step.model";
import {Feed} from "../../../../model/feed/feed.model";
import {FeedDetailsProcessorRenderingHelper} from "../../../../services/FeedDetailsProcessorRenderingHelper";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {Templates} from "../../../../../../lib/feed-mgr/services/TemplateTypes";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedNifiPropertiesService} from "../../services/feed-nifi-properties.service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";


export enum FeedDetailsMode {
    INPUT = "INPUT", ADDITIONAL = "ADDITIONAL", ALL = "ALL"
}

export class NiFiPropertiesProcessorsChangeEvent {
    constructor(public mode: FeedDetailsMode, public feed: Feed, public inputProcessors: ProcessorRef[], public nonInputProcessors: ProcessorRef[], public noPropertiesExist: boolean) {
    }
}


@Component({
    selector: "feed-nifi-properties",
    styleUrls: ["./feed-nifi-properties.component.css"],
    templateUrl: "./feed-nifi-properties.component.html"
})
export class FeedNifiPropertiesComponent implements OnInit, OnDestroy {

    @Input()
    step: Step;

    @Input()
    feed: Feed;

    @Input()
    mode?: FeedDetailsMode

    @Output()
    updatedFormControls = new EventEmitter<any>();

    @Output()
    processorsChange = new EventEmitter<NiFiPropertiesProcessorsChangeEvent>()

    @Output()
    inputProcessorChanged = new EventEmitter<ProcessorRef>()

    @Input()
    formGroup: FormGroup;

    public noPropertiesExist: boolean = false;


    form = new FormArray([]);

    formSubscription: Subscription;

    inputProcessor: ProcessorRef;

    inputProcessorControl = new FormControl('',[Validators.required]);

    inputProcessorSubscription: Subscription;

    /**
     * processors modified for display
     */
    inputProcessors: ProcessorRef[];

    nonInputProcessors: ProcessorRef[];

    constructor(private defineFeedService: DefineFeedService, stateService: StateService, private feedNifiPropertiesService: FeedNifiPropertiesService, private http: HttpClient, feedLoadingService: FeedLoadingService, dialogService: TdDialogService,
                feedSideNavService: FeedSideNavService, @Inject("RegisterTemplatePropertyService") private registerTemplatePropertyService: RegisterTemplatePropertyService) {

        this.formSubscription = this.form.statusChanges.subscribe(status => {
            if (status === "VALID" && this.form.dirty) {
                this.step.markDirty();
            }
        });
        this.inputProcessorSubscription = this.inputProcessorControl.valueChanges.subscribe((value: ProcessorRef) => {
            this.inputProcessor = value;
            this.form.setControl(0, (value != null) ? value.form : new FormControl());
            this.updateProcessors();
            this.inputProcessorChanged.emit(value)
        });
    }

    ngOnDestroy() {
        if (this.formSubscription) {
            this.formSubscription.unsubscribe();
        }
        if (this.inputProcessorSubscription) {
            this.inputProcessorSubscription.unsubscribe();
        }
    }

    ngOnInit() {
         if(this.formGroup) {
             this.formGroup.addControl("processors", this.form);
         }

         //touch the input control to show validation error
        this.inputProcessorControl.markAsTouched()

        if (this.mode == undefined) {
            this.mode = FeedDetailsMode.ALL;
        }

        this.inputProcessors = [];

        if (this.feed.isNew()) {
            this.initializeTemplateProperties();
        } else {
            this.mergeTemplateDataWithFeed(this.feed);
        }

    }

    hasProcessor(downstreamProcessors: any[], processorId: string, processorName: string): boolean {
        if (downstreamProcessors) {
            return downstreamProcessors.find(p => p.name == processorName) != undefined;
        }
        return false;
    }

    applyUpdatesToFeed(): void {

        let properties = [];
        let inputProperties: any = []; //Templates.Property[]
        let otherProperties: any = []; //Templates.Property[]

        if (this.mode == FeedDetailsMode.ALL || this.mode == FeedDetailsMode.INPUT) {
            inputProperties = this.inputProcessor.processor.properties;
            this.feed.inputProcessor = this.inputProcessor.processor as any;
            this.feed.inputProcessorName = this.inputProcessor.name;
            this.feed.inputProcessorType = this.inputProcessor.type;
        }
        else {
            this.ensureFeedInputProcessor();
            inputProperties = this.feed.inputProcessor.properties
        }

        if (this.mode == FeedDetailsMode.ALL || this.mode == FeedDetailsMode.ADDITIONAL) {
            otherProperties = _.chain(this.nonInputProcessors)
                .map(ref => ref.processor.properties as any[])
                .flatten(true)
                .value();

        } else {
            otherProperties = _.chain(this.feed.nonInputProcessors)
                .map(processor => processor.properties as any[])
                .flatten(true)
                .value();
        }

        this.feed.properties = <Templates.Property[]>inputProperties.concat(otherProperties)


    }

    private buildInputProcessorRelationships(registeredTemplate: any) {

        if (registeredTemplate.inputProcessorRelationships) {

        }
    }

    private initializeTemplateProperties() {
        if (!this.feed.propertiesInitialized && this.feed.templateId != null && this.feed.templateId != '') {
            let params = new HttpParams().append("feedEdit", "true").append("allProperties", "true");
            this.http.get(this.registerTemplatePropertyService.GET_REGISTERED_TEMPLATES_URL + "/" + this.feed.templateId, {params: params})
                .subscribe((template: any) => {
                    if (typeof this.feed.cloned !== "undefined" && this.feed.cloned == true) {
                        this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(this.feed, 'create');
                        this.feedNifiPropertiesService.sortAndSetupFeedProperties(this.feed);
                    } else {
                        this.feedNifiPropertiesService.setupFeedProperties(this.feed, template, 'create');
                        this.buildInputProcessorRelationships(template);
                        this.setProcessors(this.feed.inputProcessors, this.feed.nonInputProcessors, this.feed.inputProcessor, this.feed);
                        this.feed.propertiesInitialized = true;
                    }


                }, () => {
                });
        } else if (this.feed.propertiesInitialized) {
            this.buildInputProcessorRelationships(this.feed.registeredTemplate)
            this.setProcessors(this.feed.inputProcessors, this.feed.nonInputProcessors, this.feed.inputProcessor, this.feed);

        }
    }

    public mergeTemplateDataWithFeed(feed: Feed) {
        if (!feed.propertiesInitialized) {
            let feedCopy = feed.copy(false);
            delete feedCopy.steps;

            this.http.post<Feed>(RestUrlConstants.MERGE_FEED_WITH_TEMPLATE(feed.id), feedCopy, {headers: {'Content-Type': 'application/json; charset=UTF-8'}})
                .subscribe(updatedFeedResponse => {
                    if (updatedFeedResponse == undefined) {
                        //ERROR out
                        //@TODO present error or return observable.error()
                        console.log("Failed to merge template with undefined");
                        feed.propertiesInitialized = true;
                    } else {
                        //merge the properties back into this feed
                        feed.properties = updatedFeedResponse.properties;

                        feed.registeredTemplate = updatedFeedResponse.registeredTemplate;
                        this.feedNifiPropertiesService.setupFeedProperties(feed, feed.registeredTemplate, 'edit',);
                        feed.propertiesInitialized = true;
                        this.buildInputProcessorRelationships(feed.registeredTemplate)
                        this.setProcessors(feed.inputProcessors, feed.nonInputProcessors, feed.inputProcessor, feed);


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


                }, err => {
                    console.log("Failed to merge template", err);
                    feed.propertiesInitialized = true;
                })
        } else {
            //  this.defineFeedService.setupFeedProperties(this.feed,this.feed.registeredTemplate, 'edit')
            this.buildInputProcessorRelationships(this.feed.registeredTemplate)
            this.setProcessors(feed.inputProcessors, feed.nonInputProcessors, feed.inputProcessor, feed);

        }
    }

    private getInputRenderProcessors(inputProcessor: Templates.Processor): Templates.Processor[] {
        const renderingHelper = new FeedDetailsProcessorRenderingHelper();
        if (renderingHelper.isRenderProcessorGetTableDataProcessor(inputProcessor) || renderingHelper.isRenderSqoopProcessor(inputProcessor)) {
            const inputProcessorRelationships = (typeof this.feed.registeredTemplate.inputProcessorRelationships[inputProcessor.name] !== "undefined")
                ? this.feed.registeredTemplate.inputProcessorRelationships[inputProcessor.name] : null;
            return this.feed.nonInputProcessors
                .filter(processor => processor.id.substring(0, 18) === inputProcessorRelationships[0].id.substring(0, 18))
                .filter(processor => renderingHelper.isGetTableDataProcessor(processor) || renderingHelper.isSqoopProcessor(processor));
        } else {
            return [];
        }
    }

    private updateProcessors() {
        if(this.inputProcessor) {
            this.nonInputProcessors = this.getInputRenderProcessors(this.inputProcessor.processor as any)
                .map(processor => {
                    const ref = new ProcessorRef(processor as any, this.feed);
                    this.form.push(ref.form);
                    return ref;
                });

            let hasVisibleProcessors = _.chain([...this.inputProcessors, ...this.nonInputProcessors])
                .map(ref => ref.processor.properties)
                .flatten(true)
                .find((property: Templates.Property) => property.userEditable)
                .value() != null;

            if (!hasVisibleProcessors) {
                this.noPropertiesExist = true;
            }

            this.updatedFormControls.emit();
            this.processorsChange.emit(new NiFiPropertiesProcessorsChangeEvent(this.mode, this.feed, this.inputProcessors, this.nonInputProcessors, this.noPropertiesExist));
        }
    }
    private ensureFeedInputProcessor(){
        if(this.feed.inputProcessor == undefined && this.feed.registeredTemplate) {
            //attempt to get the first one
            //get the first input processor and select it
            let inputProcessors = this.feed.inputProcessors && this.feed.inputProcessors.length >0 ? this.feed.inputProcessors : this.feed.registeredTemplate && this.feed.registeredTemplate.inputProcessors && this.feed.registeredTemplate.inputProcessors.length >0 ? this.feed.registeredTemplate.inputProcessors : []
            if(inputProcessors.length >0) {
                let input: Templates.Processor = inputProcessors[0];
                this.feed.inputProcessor = input;
                this.feed.inputProcessorName = input.name;
                this.feed.inputProcessorType = input.type;
            }
        }
    }

    private setProcessors(inputProcessors: Templates.Processor[], nonInputProcessors: Templates.Processor[], selected?: Templates.Processor, feed?: Feed) {
        let hasVisibleProcessors = false;
        let selectedProcessorRef: ProcessorRef = null;
        if (this.isShowInputProperties()) {

            //set the value after inputProcessors is defined so the valuechanges callback is called after this.inputProcessors is initialized
            this.inputProcessors = inputProcessors.map(processor => {
                const ref = new ProcessorRef(processor as any, feed);
                if (selected && ref.id === selected.id) {
                    selectedProcessorRef = ref;
                }
                return ref;
            });


            hasVisibleProcessors = this.inputProcessors
                .find((ref: ProcessorRef) => ref.processor.properties && ref.processor.properties.find((property: Templates.Property) => property.userEditable) != undefined) != undefined;
        }
        if (this.isShowAdditionalProperties()) {
            this.ensureFeedInputProcessor()

            let inputName = this.feed.inputProcessor != undefined ? this.feed.inputProcessor.name : undefined;
            let inputProcessorRelationships = this.feed.registeredTemplate.inputProcessorRelationships;
            let downstreamProcessors = inputProcessorRelationships != undefined ? inputProcessorRelationships[inputName] : undefined;
            const inputRenderProcessors = selected ? this.getInputRenderProcessors(selected) : null;
            //limit the downstream additional processors to only those that are available in the flow coming from the input processor
            this.nonInputProcessors = nonInputProcessors
                .filter(processor => inputRenderProcessors.find(x => x.id === processor.id) == null)
                .filter(processor => downstreamProcessors != undefined ? this.hasProcessor(downstreamProcessors, processor.id, processor.name) : true)
                .map(processor => {
                    const ref = new ProcessorRef(processor as any, feed);
                    this.form.push(ref.form);
                    return ref;
                });
            if (!hasVisibleProcessors) {
                hasVisibleProcessors = this.nonInputProcessors.find((ref: ProcessorRef) => ref.processor.properties && ref.processor.properties.find((property: Templates.Property) => property.userEditable) != undefined) != undefined;
            }
        }

        if (selectedProcessorRef != null) {
            this.inputProcessorControl.setValue(selectedProcessorRef);
            this.form.setControl(0, selectedProcessorRef.form);
        }

        if (!hasVisibleProcessors) {
            this.noPropertiesExist = true;
        }
        this.updatedFormControls.emit();
        this.processorsChange.emit(new NiFiPropertiesProcessorsChangeEvent(this.mode, this.feed, this.inputProcessors, this.nonInputProcessors, this.noPropertiesExist));
    }

    private isShowInputProperties() {
        return (this.mode == FeedDetailsMode.ALL || this.mode == FeedDetailsMode.INPUT);
    }

    private isShowAdditionalProperties() {
        return (this.mode == FeedDetailsMode.ALL || this.mode == FeedDetailsMode.ADDITIONAL);
    }
}

