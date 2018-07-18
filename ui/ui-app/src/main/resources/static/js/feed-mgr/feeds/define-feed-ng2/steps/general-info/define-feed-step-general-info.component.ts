import {Component, Injector, Input, OnInit} from "@angular/core";
import {Category, DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import CategoriesService from "../../../../services/CategoriesService";
import {Observable} from "rxjs/Observable";
import {FeedStepValidator} from "../../model/feed-step-validator";
import {map, startWith,flatMap} from 'rxjs/operators';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';
import {FeedService} from "../../../../services/FeedService";
import {SaveFeedResponse} from "../../model/SaveFeedResponse";
import { MatButtonModule, MatFormFieldModule, MatInputModule } from '@angular/material';
import * as _ from 'underscore';

@Component({
    selector: "define-feed-step-general-info",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.html"
})
export class DefineFeedStepGeneralInfoComponent extends AbstractFeedStepComponent {

    @Input() stateParams :any;

    /**
     * Form control for categories autocomplete
     * @type {FormControl}
     */
    public categoryCtrl = new FormControl();

    public feedNameCtrl = new FormControl();
    /**
     * Aysnc autocomplete list of categories
     */
    public filteredCategories: Observable<Category[]>;
    /**
     * Angular 1 upgraged Categories service
     */
    private categoriesService:CategoriesService;

    private feedService: FeedService;

    /**
     * All possible schedule strategies
     * @type {*[]}
     */
    allScheduleStrategies:any = [{label: 'Cron', value: "CRON_DRIVEN"}, {label: 'Timer', value: "TIMER_DRIVEN"}, {label: 'Trigger/Event', value: "TRIGGER_DRIVEN"},
        {label: "On primary node", value: "PRIMARY_NODE_ONLY"}];


    /**
     * Array of strategies filtered for this feed
     * @type {any[]}
     */
    scheduleStrategies:any[] =[];

    /**
     * Indicates that NiFi is clustered.
     *
     * @type {boolean}
     */
    isClustered:boolean = true;

    /**
     * Indicates that NiFi supports the execution node property.
     * @type {boolean}
     */
    supportsExecutionNode:boolean = true;

    /**
     * The Timer amount with default
     * @type {number}
     */
    timerAmount:number = 5;
    /**
     * the timer units with default
     * @type {string}
     */
    timerUnits:string = "min";


    constructor(  defineFeedService:DefineFeedService,  stateService:StateService,private $$angularInjector: Injector) {
        super(defineFeedService,stateService);
        this.categoriesService = $$angularInjector.get("CategoriesService");
        this.feedService = $$angularInjector.get("FeedService");
        this.filteredCategories = this.categoryCtrl.valueChanges.flatMap(text => {
            return <Observable<Category[]>> Observable.fromPromise(this.categoriesService.querySearch(text));
        });

        //watch for changes on the feed name to generate the system name
        this.feedNameCtrl.valueChanges.debounceTime(200).subscribe(text => this.generateSystemName());
    }



    getStepName() {
        return "General Info";
    }

    init(){
     super.init();
     this.updateScheduleStrategies();

     //TODO: Detect if NiFi is clustered
    // this.http.get(this.RestUrlService)
    }


    categoryAutocompleteDisplay(category?: Category): string | undefined {
        return category ? category.name : undefined;
    }

    generateSystemName(){
        this.feedService.getSystemName(this.feed.feedName).then((response:any) => {
            this.feed.systemName = response.data;
            //TODO add in this validation
          //  this.model.table.tableSchema.name = this.model.systemFeedName;
          //  this.validateUniqueFeedName();
          //  this.validate();
        });
    }

    printDebug() {
        console.log(this.feed);
    }

    getversionFeedModel() {
        return this.feedService.versionFeedModel;
    }

    /**
     * Different templates have different schedule strategies.
     * Filter out those that are not needed based upon the template
     */
    updateScheduleStrategies() {
        this.scheduleStrategies = _.filter(this.allScheduleStrategies, (strategy:any) => {
            if (this.feed.registeredTemplate.allowPreconditions) {
                return (strategy.value === "TRIGGER_DRIVEN");
            } else if (strategy.value === "PRIMARY_NODE_ONLY") {
                return (this.isClustered && !this.supportsExecutionNode);
            } else {
                return (strategy.value !== "TRIGGER_DRIVEN");
            }
        });
    }


    cronExpressionFormControl = new FormControl('', [
        Validators.required
    ]);


    /**
     * When the timer changes show warning if its < 3 seconds indicating to the user this is a "Rapid Fire" feed
     */
    timerChanged() {
        console.log("In timerChanged() (TODO: remove this comment later)");
        if (this.timerAmount < 0) {
            this.timerAmount = null;
        }
        if (this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
            this.showTimerAlert();
        }
        this.feed.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
        this.validate();
    }

    showTimerAlert(ev?:any) {
        console.log("TODO: Implement me to show a timer alert for a rapid schedule");
    }

    validate() {
        console.log("TODO: Implement me to validate timer value");
    }

}

