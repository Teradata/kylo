import {Component, Injector, Input, OnInit, ViewChild} from "@angular/core";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {Category} from "../../../../model/category/category.model";
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import CategoriesService from "../../../../services/CategoriesService";
import {Observable} from "rxjs/Observable";
import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {map, startWith, flatMap} from 'rxjs/operators';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';
import {FeedService} from "../../../../services/FeedService";
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {MatButtonModule, MatFormFieldModule, MatInputModule} from '@angular/material';
import {MatListModule} from '@angular/material/list';
import * as _ from 'underscore';
import {NiFiService} from "../../../../services/NiFiService";
import {NiFiClusterStatus} from "../../../../model/nifi-cluster-status";
import {NiFiTimerUnit} from "../../../../model/nifi-timer-unit";
import {NiFiExecutionNodeConfiguration} from "../../../../model/nifi-execution-node-configuration";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedScheduleComponent} from "../../feed-schedule/feed-schedule.component";
import {PropertyListComponent} from "../../../../shared/property-list/property-list.component";
import {ISubscription} from "rxjs/Subscription";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import { TranslateService } from '@ngx-translate/core';
import {PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";

@Component({
    selector: "define-feed-step-general-info",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.html"
})
export class DefineFeedStepGeneralInfoComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

    formGroup:FormGroup;

    @ViewChild("feedSchedule")
    feedSchedule: FeedScheduleComponent;

    @ViewChild("propertyList")
    propertyList: PropertyListComponent;

    public title:string = "General Information";


    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;

    /**
     * Angular 1 upgraded Categories service
     */
    private categoriesService: CategoriesService;

    private feedService: FeedService;

    /**
     * Shared service with the Visual Query to store the datasets
     * Shared with Angular 1 component
     */
    previewDatasetCollectionService : PreviewDatasetCollectionService

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                feedLoadingService:FeedLoadingService,
                dialogService: TdDialogService,
                feedSideNavService:FeedSideNavService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector) {
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.categoriesService = $$angularInjector.get("CategoriesService");
        this.feedService = $$angularInjector.get("FeedService");
        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        this.formGroup = new FormGroup({});
        this.subscribeToFormChanges(this.formGroup);


    }



    init() {
        super.init();
        this.registerFormControls();
         this.subscribeToFormDirtyCheck(this.formGroup);
    }
    destroy(){

    }



    getStepName() {
        return FeedStepConstants.STEP_GENERAL_INFO;
    }

    /**
     * register form controls for the feed
     */
    private registerFormControls(){
        this.formGroup.registerControl("description", new FormControl(this.feed.description));
        this.formGroup.registerControl("dataOwner", new FormControl(this.feed.dataOwner));
    }

    /**
     * Update the feed model with the form values
     */
   protected  applyUpdatesToFeed():(Observable<any>| null){
       //update the model
     let formModel =   this.formGroup.value;
     this.feed.feedName = formModel.feedName;
     this.feed.systemFeedName = formModel.systemFeedName;
     this.feed.category = formModel.category;
     this.feed.description = formModel.description;
     this.feed.dataOwner = formModel.dataOwner;
     if(this.feedSchedule) {
         this.feedSchedule.updateModel();
     }
     if(this.propertyList) {
         this.propertyList.updateModel();
     }

     return null;
    }

    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit(){
         this.propertyList.reset(this.feed.userProperties);
        this.feedSchedule.reset(this.feed);
    }
}