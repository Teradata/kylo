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


    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;

    /**
     * Angular 1 upgraded Categories service
     */
    private categoriesService: CategoriesService;

    private feedService: FeedService;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector) {
        super(defineFeedService, stateService);
        this.categoriesService = $$angularInjector.get("CategoriesService");
        this.feedService = $$angularInjector.get("FeedService");
        this.formGroup = new FormGroup({});
        this.subscribeToFormChanges(this.formGroup);


    }



    init() {
        super.init();
        this.registerFormControls();
    }
    destroy(){

    }

    /**
     * Function for the Autocomplete to display the name of the category object matched
     * @param {Category} category
     * @return {string | undefined}
     */
    categoryAutocompleteDisplay(category?: Category): string | undefined {
        return category ? category.name : undefined;
    }


    getStepName() {
        return FeedStepConstants.STEP_GENERAL_INFO;
    }

    /**
     * register form controls for the feed
     */
    private registerFormControls(){
        let feedNameCtrl = new FormControl(this.feed.feedName,[Validators.required])
        feedNameCtrl.valueChanges.debounceTime(200).subscribe(value => {
            this.generateSystemName();
        });
        this.formGroup.registerControl("feedName",feedNameCtrl);

        //TODO add in pattern validator, and unique systemFeedName validator
        this.formGroup.registerControl("systemFeedName", new FormControl(this.feed.systemFeedName,[Validators.required]))

        let categoryCtrl = new FormControl(this.feed.category,[Validators.required])
        this.formGroup.registerControl("category",categoryCtrl);
        this.filteredCategories = categoryCtrl.valueChanges.flatMap(text => {
            return <Observable<Category[]>> Observable.fromPromise(this.categoriesService.querySearch(text));
        });

        this.formGroup.registerControl("description", new FormControl(this.feed.description));
        this.formGroup.registerControl("dataOwner", new FormControl(this.feed.dataOwner));


    }



    private generateSystemName() {
        this.feedService.getSystemName(this.feed.feedName).then((response: any) => {
            this.formGroup.get("systemFeedName").setValue(this.feed.systemFeedName);
            //TODO add in this validation
            //  this.model.table.tableSchema.name = this.model.systemFeedName;
            //  this.validateUniqueFeedName();
            //  this.validate();
        });
    }


    /**
     * Update the feed model with the form values
     */
    updateFeedService(){
       //update the model
     let formModel =   this.formGroup.value;
     this.feed.feedName = formModel.feedName;
     this.feed.systemFeedName = formModel.systemFeedName;
     this.feed.category = formModel.category;
     this.feed.description = formModel.description;
     this.feed.dataOwner = formModel.dataOwner;
     this.feedSchedule.updateModel();
     this.propertyList.updateModel();
     //save it back to the service
     super.updateFeedService();
    }

    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit(feed:Feed){
        this.propertyList.reset(feed.userProperties);
        this.feedSchedule.reset(feed);
    }
}