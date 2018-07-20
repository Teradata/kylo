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
import {map, startWith, flatMap} from 'rxjs/operators';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/map';
import {FeedService} from "../../../../services/FeedService";
import {SaveFeedResponse} from "../../model/SaveFeedResponse";
import {MatButtonModule, MatFormFieldModule, MatInputModule} from '@angular/material';
import {MatListModule} from '@angular/material/list';
import * as _ from 'underscore';
import {NiFiService} from "../../../../services/NiFiService";
import {NiFiClusterStatus} from "../../../../model/nifi-cluster-status";
import {NiFiTimerUnit} from "../../../../model/nifi-timer-unit";
import {NiFiExecutionNodeConfiguration} from "../../../../model/nifi-execution-node-configuration";
import {TdDialogService} from "@covalent/core/dialogs";

@Component({
    selector: "define-feed-step-general-info",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/general-info/define-feed-step-general-info.component.html"
})
export class DefineFeedStepGeneralInfoComponent extends AbstractFeedStepComponent {

    @Input() stateParams: any;

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
     * Angular 1 upgraded Categories service
     */
    private categoriesService: CategoriesService;

    private feedService: FeedService;

    /**
     * All possible schedule strategies
     * @type {*[]}
     */
    allScheduleStrategies: any = [
        {label: 'Cron', value: "CRON_DRIVEN"},
        {label: 'Timer', value: "TIMER_DRIVEN"},
        {label: 'Trigger/Event', value: "TRIGGER_DRIVEN"},
        {label: "On primary node", value: "PRIMARY_NODE_ONLY"}
    ];

    /**
     * Array of strategies filtered for this feed
     * @type {any[]}
     */
    scheduleStrategies: any[] = [];

    /**
     * Indicates that NiFi is clustered.
     * @type {boolean}
     */
    isClustered: boolean = false;

    /**
     * Indicates that NiFi supports the execution node property.
     * @type {boolean}
     */
    supportsExecutionNode: boolean = false;

    /**
     * The Timer amount with default
     * @type {number}
     */
    timerAmount: number = 5;

    /**
     * the timer units with default
     * @type {string}
     */
    timerUnits: string = "min";

    /**
     * Last known schedulingPeriod (cron)
     * @type {string}
     */
    lastKnownCron: string;

    /**
     * Last known timer amount. To be used along with {@link lastKnownTimerUnits}
     * @type {number}
     */
    lastKnownTimerAmount: number;

    /**
     * Last known timer units. To be used along with {@link lastKnownTimerAmount}
     * @type {string}
     */
    lastKnownTimerUnits: string;

    /**
     * NiFi Timer Units
     * @type {NiFiTimerUnit[]}
     */
    private nifiTimerUnits: NiFiTimerUnit[] = [
        {value: 'days', description: 'Days'},
        {value: 'hrs', description: 'Hours'},
        {value: 'min', description: 'Minutes'},
        {value: 'sec', description: 'Seconds'}
    ];

    /**
     * NiFi Execution Node Configuration
     * @type {NiFiExecutionNodeConfiguration[]}
     */
    private nifiExecutionNodeConfigurations: NiFiExecutionNodeConfiguration[] = [
        {value: 'ALL', description: 'All nodes'},
        {value: 'PRIMARY', description: 'Primary node'},
    ];

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                private $$angularInjector: Injector,
                private nifiService: NiFiService,
                private dialogService: TdDialogService) {
        super(defineFeedService, stateService);
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

    init() {
        super.init();
        this.updateScheduleStrategies();
        this.detectNiFiClusterStatus();

        if (this.feed.schedule.schedulingStrategy == "TIMER_DRIVEN" || this.feed.schedule.schedulingStrategy === "PRIMARY_NODE_ONLY") {
            this.parseTimer();
        } else {
            this.parseCron();
        }
    }

    /**
     * Get info on NiFi clustering
     */
    detectNiFiClusterStatus() {
        this.nifiService.getNiFiClusterStatus().subscribe((nifiClusterStatus: NiFiClusterStatus) => {
            if (nifiClusterStatus.clustered != null) {
                this.isClustered = nifiClusterStatus.clustered;
            } else {
                this.isClustered = false;
            }

            if (nifiClusterStatus.version != null) {
                this.supportsExecutionNode = ((this.isClustered) && (!nifiClusterStatus.version.match(/^0\.|^1\.0/)));
            } else {
                this.supportsExecutionNode = false;
            }
        });
    }

    categoryAutocompleteDisplay(category?: Category): string | undefined {
        return category ? category.name : undefined;
    }

    generateSystemName() {
        this.feedService.getSystemName(this.feed.feedName).then((response: any) => {
            this.feed.systemName = response.data;
            //TODO add in this validation
            //  this.model.table.tableSchema.name = this.model.systemFeedName;
            //  this.validateUniqueFeedName();
            //  this.validate();
        });
    }

    getversionFeedModel() {
        return this.feedService.versionFeedModel;
    }

    /**
     * The model stores the timerAmount and timerUnits together as 1 string.
     * This will parse that string and set each variable in the component
     * Also, note the last value of the timer amount and timer units
     */
    parseTimer() {
        this.timerAmount = parseInt(this.feed.schedule.schedulingPeriod);
        var startIndex = this.feed.schedule.schedulingPeriod.indexOf(" ");
        if (startIndex != -1) {
            this.timerUnits = this.feed.schedule.schedulingPeriod.substring(startIndex + 1);
        }

        this.lastKnownTimerAmount = this.timerAmount;
        this.lastKnownTimerUnits = this.timerUnits;
    }

    /**
     * Note the last value of cron schedule
     */
    parseCron() {
        this.lastKnownCron = this.feed.schedule.schedulingPeriod;
    }

    /**
     * When the timer changes show warning if it is < 3 seconds indicating to the user this is a "Rapid Fire" feed
     */
    timerChanged() {
        if (this.timerAmount < 0) {
            this.timerAmount = null;
        }
        if (this.timerAmount != null && (this.timerAmount == 0 || (this.timerAmount < 3 && this.timerUnits == 'sec'))) {
            this.showTimerAlert();
        }
        this.feed.schedule.schedulingPeriod = this.timerAmount + " " + this.timerUnits;
        this.validate();
    }

    /**
     * Show alert for a rapid timer.
     * @param ev
     */
    showTimerAlert(ev?: any) {
        this.dialogService.openAlert({
            message: 'Warning: You have this feed scheduled for a very fast timer. Please ensure you want this feed scheduled this fast before you proceed.',
            disableClose: true,
            title: 'Warning: Rapid Timer',
            closeButton: 'Close',
            width: '200 px',
        });
    }

    /**
     * Logic to set values when schedule strategy is changed
     */
    onScheduleStrategyChange() {
        if (this.feed.schedule.schedulingStrategy == "CRON_DRIVEN") {
            if (this.feed.schedule.schedulingPeriod != this.feedService.DEFAULT_CRON) {
                this.setCronDriven();
            }
        } else if (this.feed.schedule.schedulingStrategy == "TIMER_DRIVEN") {
            this.setTimerDriven();
        } else if (this.feed.schedule.schedulingStrategy == "PRIMARY_NODE_ONLY") {
            this.setPrimaryNodeOnly();
        }
    }

    /**
     * Force the model to be set to Cron using default or last known value
     */
    setCronDriven() {
        this.feed.schedule.schedulingStrategy = 'CRON_DRIVEN';

        if (this.lastKnownCron != null) {
            this.feed.schedule.schedulingPeriod = this.lastKnownCron;
        } else {
            this.feed.schedule.schedulingPeriod = this.feedService.DEFAULT_CRON;
        }
    }

    /**
     * Force the model and timer to be set to Timer with the defaults or last known value
     */
    setTimerDriven() {
        this.feed.schedule.schedulingStrategy = 'TIMER_DRIVEN';

        if ((this.lastKnownTimerAmount != null) && (this.lastKnownTimerUnits != null)) {
            this.timerAmount = this.lastKnownTimerAmount;
            this.timerUnits = this.lastKnownTimerUnits;
            this.feed.schedule.schedulingPeriod = this.lastKnownTimerAmount + " " + this.lastKnownTimerUnits;
        } else {
            this.timerAmount = 5;
            this.timerUnits = "min";
            this.feed.schedule.schedulingPeriod = "5 min";
        }
    }

    /**
     * Set the scheduling strategy to 'On primary node'.
     */
    setPrimaryNodeOnly() {
        this.feed.schedule.schedulingStrategy = "PRIMARY_NODE_ONLY";
        this.timerAmount = 5;
        this.timerUnits = "min";
        this.feed.schedule.schedulingPeriod = "5 min";
    }

    /**
     * Different templates have different schedule strategies.
     * Filter out those that are not needed based upon the template
     */
    updateScheduleStrategies() {
        this.scheduleStrategies = _.filter(this.allScheduleStrategies, (strategy: any) => {
            if (this.feed.registeredTemplate.allowPreconditions) {
                return (strategy.value === "TRIGGER_DRIVEN");
            } else if (strategy.value === "PRIMARY_NODE_ONLY") {
                return (this.isClustered && !this.supportsExecutionNode);
            } else {
                return (strategy.value !== "TRIGGER_DRIVEN");
            }
        });
    }


    showPreconditionDialog(index: any) {
        //TODO: To be implemented
        console.log("Implement me to show precondition dialog. Index is ", index);
    }

    /**
     * Validates the inputs are good
     */
    validate() {
        //TODO: To be implemented
        console.log("Implement me to validate the form");
    }

    /**
     * Get NiFi timer units
     * @returns {NiFiTimerUnit[]}
     */
    getNiFiTimerUnits(): NiFiTimerUnit[] {
        return this.nifiTimerUnits;
    }

    /**
     * Get NiFi execution node configuration
     * @returns {NiFiExecutionNodeConfiguration[]}
     */
    getNiFiExecutionNodeConfigurations(): NiFiExecutionNodeConfiguration[] {
        return this.nifiExecutionNodeConfigurations;
    }
}