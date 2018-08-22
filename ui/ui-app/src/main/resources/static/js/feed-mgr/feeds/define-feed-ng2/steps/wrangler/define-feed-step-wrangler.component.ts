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

import {QueryEngineFactory} from "../../../../visual-query/wrangler/query-engine-factory.service";
import {VisualQueryStepperComponent} from "../../../../visual-query/visual-query-stepper.component";

@Component({
    selector: "define-feed-step-wrangler",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/wrangler/define-feed-step-wrangler.component.html"
})
export class DefineFeedStepWranglerComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

    formGroup:FormGroup;

    @ViewChild("visualQuery")
    visualQuery: VisualQueryStepperComponent;

    /**
     * Shared service with the Visual Query to store the datasets
     * Shared with Angular 1 component
     */
    previewDatasetCollectionService : PreviewDatasetCollectionService

    queryEngine: QueryEngineFactory;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private _translateService: TranslateService,
                private $$angularInjector: Injector) {
        super(defineFeedService, stateService);
        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        this.formGroup = new FormGroup({});
        this.queryEngine = $$angularInjector.get("VisualQueryEngineFactory").getEngine("spark")
        this.subscribeToFormChanges(this.formGroup);

    }



    init() {
        super.init();

        this.registerFormControls();
    }
    destroy(){

    }



    getStepName() {
        return FeedStepConstants.STEP_WRANGLER;
    }

    /**
     * register form controls for the feed
     */
    private registerFormControls(){


    }




    /**
     * Update the feed model with the form values
     */
    updateFeedService(){
       //update the model
    // let formModel =   this.formGroup.value;

     //this.feed.  = formModel.

     //save it back to the service
     super.updateFeedService();
    }

    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit(feed:Feed){

    }
}