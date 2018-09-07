import {HttpClient, HttpParams} from "@angular/common/http";
import {Component, Inject} from "@angular/core";
import {FormArray, FormControl} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {StateService} from "@uirouter/angular";
import {Subscription} from "rxjs/Subscription";
import * as _ from "underscore";

import {ProcessorRef} from "../../../../../../lib/feed/processor/processor-ref";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {Feed} from "../../../../model/feed/feed.model";
import {RegisterTemplatePropertyService} from "../../../../services/RegisterTemplatePropertyService";
import {RestUrlConstants} from "../../../../services/RestUrlConstants";
import {Templates} from "../../../../services/TemplateTypes";
import {DefineFeedService} from "../../services/define-feed.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";

@Component({
    selector: "define-feed-step-feed-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.html"
})
export class DefineFeedStepFeedDetailsComponent extends AbstractFeedStepComponent {

    form = new FormArray([]);

    formSubscription: Subscription;

    inputProcessor: ProcessorRef;

    inputProcessorControl = new FormControl();

    inputProcessorSubscription: Subscription;

    /**
     * processors modified for display
     */
    inputProcessors: ProcessorRef[];

    nonInputProcessors: ProcessorRef[];

    constructor(defineFeedService: DefineFeedService, stateService: StateService, private http: HttpClient, feedLoadingService: FeedLoadingService, dialogService: TdDialogService,
                feedSideNavService: FeedSideNavService, @Inject("RegisterTemplatePropertyService") private registerTemplatePropertyService: RegisterTemplatePropertyService) {
        super(defineFeedService, stateService, feedLoadingService, dialogService, feedSideNavService);

        this.formSubscription = this.form.statusChanges.subscribe(status => {
            if (status === "VALID" && this.form.dirty) {
                this.step.markDirty();
            }
        });
        this.inputProcessorSubscription = this.inputProcessorControl.valueChanges.subscribe((value: ProcessorRef) => {
            this.inputProcessor = value;
            this.form.setControl(0, (value != null) ? value.form : new FormControl());
        });
    }

    destroy() {
        super.destroy();

        if (this.formSubscription) {
            this.formSubscription.unsubscribe();
        }
        if (this.inputProcessorSubscription) {
            this.inputProcessorSubscription.unsubscribe();
        }
    }

    getStepName() {
        return FeedStepConstants.STEP_FEED_DETAILS;
    }

    init() {
        this.inputProcessors = [];

        if (this.feed.isNew()) {
            this.initializeTemplateProperties();
        } else {
            this.mergeTemplateDataWithFeed(this.feed);
        }

        //listen when the form is valid or invalid
        this.subscribeToFormChanges(this.form);
    }


    protected applyUpdatesToFeed(): void {
        super.applyUpdatesToFeed();

        this.feed.properties = _.chain([this.inputProcessor, ...this.nonInputProcessors])
            .map(ref => ref.processor.properties as any[])
            .flatten(true)
            .value();
        this.feed.inputProcessor = this.inputProcessor.processor as any;
        this.feed.inputProcessorName = this.inputProcessor.name;
        this.feed.inputProcessorType = this.inputProcessor.type;
    }

    private initializeTemplateProperties() {
        if (!this.feed.propertiesInitialized && this.feed.templateId != null && this.feed.templateId != '') {
            let params = new HttpParams().append("feedEdit", "true").append("allProperties", "true");
            this.http.get(this.registerTemplatePropertyService.GET_REGISTERED_TEMPLATES_URL + "/" + this.feed.templateId, {params: params})
                .subscribe((template: any) => {
                    if (typeof this.feed.cloned !== "undefined" && this.feed.cloned == true) {
                        this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(this.feed, 'create');
                        this.defineFeedService.sortAndSetupFeedProperties(this.feed);
                    } else {
                        this.defineFeedService.setupFeedProperties(this.feed, template, 'create');
                        this.setProcessors(this.feed.inputProcessors, this.feed.nonInputProcessors, this.feed.inputProcessor, this.feed);
                        this.feed.propertiesInitialized = true;

                        this.subscribeToFormDirtyCheck(this.form);
                    }

                }, () => {
                });
        } else if (this.feed.propertiesInitialized) {
            this.setProcessors(this.feed.inputProcessors, this.feed.nonInputProcessors, this.feed.inputProcessor, this.feed);
            this.subscribeToFormDirtyCheck(this.form);
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
                    } else {
                        //merge the properties back into this feed
                        feed.properties = updatedFeedResponse.properties;
                        feed.inputProcessors = updatedFeedResponse.inputProcessors;
                        feed.nonInputProcessors = updatedFeedResponse.nonInputProcessors;
                        feed.registeredTemplate = updatedFeedResponse.registeredTemplate;
                        this.defineFeedService.setupFeedProperties(feed, feed.registeredTemplate, 'edit');
                        feed.propertiesInitialized = true;
                        this.setProcessors(feed.inputProcessors, feed.nonInputProcessors, feed.inputProcessor, feed);
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
        } else {
            //  this.defineFeedService.setupFeedProperties(this.feed,this.feed.registeredTemplate, 'edit')
            this.setProcessors(feed.inputProcessors, feed.nonInputProcessors, feed.inputProcessor, feed);
            this.subscribeToFormDirtyCheck(this.form);
        }
    }

    private setProcessors(inputProcessors: Templates.Processor[], nonInputProcessors: Templates.Processor[], selected?: Templates.Processor, feed?: Feed) {
        this.inputProcessors = inputProcessors.map(processor => {
            const ref = new ProcessorRef(processor as any, feed);
            if (ref.id === selected.id) {
                this.inputProcessorControl.setValue(ref);
                this.form.setControl(0, ref.form);
            }
            return ref;
        });
        this.nonInputProcessors = nonInputProcessors.map(processor => {
            const ref = new ProcessorRef(processor as any, feed);
            this.form.push(ref.form);
            return ref;
        });
    }
}
