import {NiFiExecutionNodeConfiguration} from "../../../../../model/nifi-execution-node-configuration";
import {NiFiTimerUnit} from "../../../../../model/nifi-timer-unit";
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {Component, Input, OnDestroy, OnInit, ViewContainerRef} from "@angular/core";
import {Feed} from "../../../../../model/feed/feed.model";
import {NiFiClusterStatus} from "../../../../../model/nifi-cluster-status";
import {TdDialogService} from "@covalent/core/dialogs";
import {NiFiService} from "../../../../../services/NiFiService";
import {FeedConstants} from "../../../../../services/FeedConstants";
import * as _ from "underscore";
import {FeedPreconditionDialogService} from "../../../../../shared/feed-precondition/feed-precondition-dialog-service";
import {CloneUtil} from "../../../../../../common/utils/clone-util";

@Component({
    selector: "feed-schedule",
    templateUrl: "./feed-schedule.component.html"
})
export class FeedScheduleComponent implements OnInit, OnDestroy{


    @Input()
    public feed:Feed;

    @Input()
    public editable:boolean;

    @Input()
    public parentForm:FormGroup;

    public scheduleForm:FormGroup;

    /**
     * All possible schedule strategies
     * @type {*[]}
     */
    allScheduleStrategies: any = [];

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
     * copy of the preconditions that will be modified and applied back to the feed on save
     */
    preconditions:any[];



    /**
     * NiFi Timer Units
     * @type {NiFiTimerUnit[]}
     */
    public nifiTimerUnits: NiFiTimerUnit[] = [
        {value: 'days', description: 'Days'},
        {value: 'hrs', description: 'Hours'},
        {value: 'min', description: 'Minutes'},
        {value: 'sec', description: 'Seconds'}
    ];

    /**
     * NiFi Execution Node Configuration
     * @type {NiFiExecutionNodeConfiguration[]}
     */
    public nifiExecutionNodeConfigurations: NiFiExecutionNodeConfiguration[] = [
        {value: 'ALL', description: 'All nodes'},
        {value: 'PRIMARY', description: 'Primary node'},
    ];

    constructor( private nifiService: NiFiService,
                 private dialogService: TdDialogService,
                 private preconditionDialogService: FeedPreconditionDialogService,
                 private _viewContainerRef:ViewContainerRef) {
        this.scheduleForm = new FormGroup({});
       this.allScheduleStrategies = Object.keys(FeedConstants.SCHEDULE_STRATEGIES).map(key => FeedConstants.SCHEDULE_STRATEGIES[key])

        let cronExpressionValue = FeedConstants.DEFAULT_CRON;
        let cronExpressionControl = new FormControl(cronExpressionValue, [Validators.required]);
        this.scheduleForm.registerControl("cronExpression", cronExpressionControl);
    }


    ngOnInit(){

        this.detectNiFiClusterStatus();

        if(this.parentForm){
            this.parentForm.registerControl("scheduleForm",this.scheduleForm);
        }
        this.preconditions = this.feed.schedule.preconditions != undefined ? this.feed.schedule.preconditions : [];
        this.initializFormControls();
        this.updateScheduleStrategies();
    }

    ngOnDestroy() {

    }

    /**
     * Check for the form control value and set it to the incoming value if present
     * @param {string} formControlName
     * @param value
     * @return {boolean} true if the control exists and was set, false if not
     */
    private checkAndSetValue(formControlName:string,value:any):boolean {
     let control = this.scheduleForm.get(formControlName);
     if(control) {
         control.setValue(value);
     }
     return control != undefined;
    }


    /**
     * Check if the given control and validationKey has an error
     * @param {string} name
     * @param {string} check
     * @return {boolean}
     */
    hasError(name:string,check:string){
        let control = this.scheduleForm.get(name);
        if(control){
            return control.hasError(check);
        }
        else {
            return false;
        }
    }

    private initializFormControls(){
        if(!this.checkAndSetValue("scheduleStrategy",this.feed.schedule.schedulingStrategy)) {
            let scheduleStrategyControl = new FormControl(this.feed.schedule.schedulingStrategy, [Validators.required]);
            scheduleStrategyControl.valueChanges.subscribe(value => {
                this.registerFormControls();
            })
            this.scheduleForm.registerControl("scheduleStrategy", scheduleStrategyControl);
        }
        this.registerFormControls();
    }

    /**
     * register form controls with the feed values
     */
   private registerFormControls(){
       let cronExpressionValue = FeedConstants.DEFAULT_CRON;
       let timerAmountValue = 5;
       let timerUnitsValue  = 'min';

       if (this.feed.schedule.schedulingStrategy == FeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.value || this.feed.schedule.schedulingStrategy === FeedConstants.SCHEDULE_STRATEGIES.PRIMARY_NODE_ONLY.value) {
         cronExpressionValue = FeedConstants.DEFAULT_CRON;
         timerAmountValue = this.parseTimerAmount();
         timerUnitsValue = this.parseTimerUnits();
       } else if(this.feed.schedule.schedulingStrategy == FeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.value){
           cronExpressionValue = this.feed.schedule.schedulingPeriod;
       }

       this.checkAndSetValue("cronExpression",cronExpressionValue);

       if(!this.checkAndSetValue("timerAmount",timerAmountValue)) {
            let timerAmountFormControl = new FormControl(timerAmountValue, [
                Validators.required,
                this.timerAmountValidator(this.feed.registeredTemplate != undefined ? this.feed.registeredTemplate.isStream : false), //TODO pass in the 'isStream' flag when selecting a template for a new feed and populate or push it on the feed object for this check
                Validators.min(0)
            ]);

            this.scheduleForm.registerControl("timerAmount", timerAmountFormControl);
        }

       if(!this.checkAndSetValue("timerUnits",timerUnitsValue)) {
            let timerUnitsFormControl = new FormControl(timerUnitsValue, [Validators.required]);
            this.scheduleForm.registerControl("timerUnits", timerUnitsFormControl)
        }

        if(this.isTriggerDriven()) {
            let preconditions = this.preconditions.map(precondition => precondition.propertyValuesDisplayString).join(",");
            if(!this.checkAndSetValue("preconditions",preconditions)){
                this.scheduleForm.addControl("preconditions", new FormControl(preconditions,[Validators.required]));
            }
        }
        else {
            if(this.scheduleForm.contains("preconditions")){
                this.scheduleForm.removeControl("preconditions");
            }
        }

    }

    /**
     * Show alert for a rapid timer for batch feed.
     * @param ev
     */
    private showBatchTimerAlert(ev?: any) {
        this.dialogService.openAlert({
            message: 'Warning: This is a batch-type feed, and scheduling for a very fast timer is not permitted. Please modify the timer amount to a non-zero value.',
            disableClose: true,
            title: 'Warning: Rapid Timer (Batch Feed)',
            closeButton: 'Close',
            width: '200 px',
        });
    }

    public timerChanged(){

    }


    /**
     * Save the form back to the feed
     */
    updateModel(): Feed{
        let formModel = this.scheduleForm.value;
        let scheduleStrategyValue = this.scheduleForm.get("scheduleStrategy").value;
        this.feed.schedule.schedulingStrategy = formModel.scheduleStrategy;
        if(this.isCronDriven()){
            this.feed.schedule.schedulingPeriod = formModel.cronExpression;
        }
        else if(this.isTriggerDriven()) {
            this.feed.schedule.preconditions = this.preconditions;
        }
        else {
            this.feed.schedule.schedulingPeriod = formModel.timerAmount+" "+formModel.timerUnits;
        }
        return this.feed;
    }

    /**
     * Resets the form with the feed values
     * @param {Feed} feed
     */
    reset(feed:Feed){
        this.preconditions = this.feed.schedule.preconditions != undefined ? this.feed.schedule.preconditions : [];
        this.registerFormControls();
    }

    /**
     * Get info on NiFi clustering
     */
    private detectNiFiClusterStatus() {
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


    /**
     * returns the timer amount numeric value (i.e. for 5 hrs it will return 5)
     * @return {number}
     */
    private parseTimerAmount() : number {
       return parseInt(this.feed.schedule.schedulingPeriod);
    }

    /**
     * returns the timer units (i.e. for 5 hrs it will return 'hrs')
     * @return {string}
     */
    private parseTimerUnits() : string {
        let timerUnits = "min"
        var startIndex = this.feed.schedule.schedulingPeriod.indexOf(" ");
        if (startIndex != -1) {
            timerUnits = this.feed.schedule.schedulingPeriod.substring(startIndex + 1);
        }
        return timerUnits;
    }

    /**
     * Different templates have different schedule strategies.
     * Filter out those that are not needed based upon the template
     */
   private updateScheduleStrategies() {
        this.scheduleStrategies = _.filter(this.allScheduleStrategies, (strategy: any) => {
            if (this.feed && this.feed.registeredTemplate && this.feed.registeredTemplate.allowPreconditions) {
                return (strategy.value === FeedConstants.SCHEDULE_STRATEGIES.TRIGGER_DRIVEN.value);
            } else if (strategy.value === FeedConstants.SCHEDULE_STRATEGIES.PRIMARY_NODE_ONLY.value) {
                return (this.isClustered && !this.supportsExecutionNode);
            } else {
                return (strategy.value !== FeedConstants.SCHEDULE_STRATEGIES.TRIGGER_DRIVEN.value);
            }
        });
        let currentStrategy =  this.getScheduleStrategy();
        if(this.scheduleStrategies.find((strategy:any) => strategy.value ==currentStrategy ) == undefined){
            this.resetScheduleStrategy();
        }
    }


   private showPreconditionDialog(index: any) {
        this.preconditionDialogService.openDialog({feed: this.feed, preconditions:this.preconditions, itemIndex: index}, this._viewContainerRef).subscribe((preconditions:any[]) => {
            let preconditionsString =preconditions.map(precondition => precondition.name).join(",");
            this.checkAndSetValue("preconditions",preconditionsString)

            this.preconditions = preconditions;
       });
   }

    /**
     * Validates the inputs are good
     */
   public validate() {
       if(this.feed) {
           if (FeedConstants.SCHEDULE_STRATEGIES.TRIGGER_DRIVEN.value == this.feed.schedule.schedulingStrategy) {
               return this.preconditions.length >0;
           }
           else if (!this.editable) {
               this.feed.schedule.schedulingPeriod != null
           }
           else {
               return this.scheduleForm.valid;
           }
       }
       else {
           return false;
       }
    }


    /**
     * Custom validator for timer amount
     * @param {boolean} isStreamingFeed
     * @returns {ValidatorFn}
     */
    private timerAmountValidator(isStreamingFeed: boolean): ValidatorFn {
        return (control: AbstractControl): { [key: string]: boolean } | null => {
            if (isStreamingFeed === false && control.value != null && control.value == 0) {
                return { 'batchFeedRequiresNonZeroTimerAmount': true };
            }
            return null;
        }
    };

    private resetScheduleStrategy(){
        let scheduleStrategy = this.scheduleForm.get("scheduleStrategy");
        if(scheduleStrategy) {
            scheduleStrategy.setValue(null);
        }
    }

    private  getScheduleStrategy(){
        let scheduleStrategy = this.scheduleForm.get("scheduleStrategy");
        if(scheduleStrategy) {
            return scheduleStrategy.value
        }else {
           return null; // return FeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.value;
        }
    }

    public isCronDriven() :boolean {
        return FeedConstants.SCHEDULE_STRATEGIES.CRON_DRIVEN.value == this.getScheduleStrategy()
    }
    public isTimerDriven():boolean {
        return  FeedConstants.SCHEDULE_STRATEGIES.TIMER_DRIVEN.value == this.getScheduleStrategy()
    }

    public isPrimaryNodeOnly():boolean {
        return FeedConstants.SCHEDULE_STRATEGIES.PRIMARY_NODE_ONLY.value == this.getScheduleStrategy()
    }

    public isTriggerDriven():boolean {
        return FeedConstants.SCHEDULE_STRATEGIES.TRIGGER_DRIVEN.value == this.getScheduleStrategy()
    }

}