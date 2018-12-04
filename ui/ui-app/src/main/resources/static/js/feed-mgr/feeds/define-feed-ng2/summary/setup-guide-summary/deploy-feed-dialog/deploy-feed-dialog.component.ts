import {Component, Inject, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {FileUpload} from "../../../../../../services/FileUploadService";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {RestUrlConstants} from "../../../../../services/RestUrlConstants";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../../../model/feed/feed-constants";
import {DefineFeedService} from "../../../services/define-feed.service";
import {StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";
import {FeedScheduleComponent} from "../feed-schedule/feed-schedule.component";
import {FormControl, FormGroup} from "@angular/forms";
import {SaveFeedResponse} from "../../../model/save-feed-response.model";
import {FeedLoadingService} from "../../../services/feed-loading-service";
import {Templates} from "../../../../../../../lib/feed-mgr/services/TemplateTypes";
import {FeedStepConstants} from "../../../../../model/feed/feed-step-constants";
import {DeployEntityVersionResponse} from "../../../../../model/deploy-entity-response.model";
import {NifiErrorMessage} from "../../../../../model/nifi-error-message.model";
import {HttpErrorResponse} from "@angular/common/http";

export class DeployFeedDialogComponentData{
    constructor(public feed:Feed){

    }
}

@Component({
    selector:"deploy-feed-dialog",
    templateUrl: "./deploy-feed-dialog.component.html"
})
export class DeployFeedDialogComponent implements OnInit, OnDestroy{

    formGroup:FormGroup;


    @ViewChild("feedSchedule")
    feedSchedule: FeedScheduleComponent;

    feed:Feed;

    deployingFeed:boolean;

    deployError:boolean;

    deployErrorMessage:string;

    deployEntityErrors:NifiErrorMessage[];

    constructor(private dialog: MatDialogRef<DeployFeedDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: DeployFeedDialogComponentData, private defineFeedService:DefineFeedService, private  stateService: StateService) {
        this.feed = this.data.feed;
        this.formGroup = new FormGroup({});

    }


    ngOnInit() {
        this.formGroup.addControl("enableFeed",new FormControl(this.feed.active))
    }
    ngOnDestroy(){

    }

    deployFeed(){
        this.deployError = false;
        this.deployErrorMessage = '';
        this.deployEntityErrors = []

        let deploy = () => {
            this.deployingFeed = true;
            this.defineFeedService.deployFeed(this.data.feed).subscribe((response:any) =>{
                if(response){
                    this.dialog.close();
                    this.deployingFeed = false;
                    let  redirectState = FEED_DEFINITION_SUMMARY_STATE_NAME+".feed-activity";
                    this.stateService.go(redirectState,{feedId:this.data.feed.id,refresh:true}, {location:'replace'})
                }
                else {
                    this.deployingFeed = false;
                    this.deployError = true;
                    this.deployErrorMessage= "There was an error deploying your feed";
                }
            }, (error1:any) => {
                this.deployingFeed = false;
                this.deployError = true;
                this.deployErrorMessage= "There was an error deploying your feed";
                if(error1 && error1 instanceof HttpErrorResponse){
                    let errorResponse = (<HttpErrorResponse>error1);
                    if(errorResponse.error && errorResponse.error.errors){
                        let content = errorResponse.error as DeployEntityVersionResponse;
                        Object.keys(content.errors.errorMap).forEach(key => {
                            let errorArray = content.errors.errorMap[key];
                            console.log('errors ',errorArray,'for key ',key)
                            if(errorArray){
                                errorArray.forEach(err => this.deployEntityErrors.push(err));
                            }
                        });
                        this.deployErrorMessage = content.errors.message
                    }
                    console.log("deployEntityErrors ", this.deployEntityErrors,'msg ', this.deployErrorMessage)
                }
            })
        }

        //if we are not showing the source
        let dirtyForm = this.formGroup.dirty
        let inputProcessorAssignmentNeeded = this.isUndefined(this.feed.inputProcessorName) || this.isUndefined(this.feed.inputProcessorType);




        if(dirtyForm || inputProcessorAssignmentNeeded) {
            this.deployingFeed = true;
            //if user chooses to upate the model save first
            let feed = this.feedSchedule.updateModel();
            feed.active = this.formGroup.get("enableFeed").value

            if(inputProcessorAssignmentNeeded) {
                //get the first input processor and select it
                let inputProcessors = feed.inputProcessors && feed.inputProcessors.length >0 ? feed.inputProcessors : feed.registeredTemplate && feed.registeredTemplate.inputProcessors && feed.registeredTemplate.inputProcessors.length >0 ? feed.registeredTemplate.inputProcessors : []
                if(inputProcessors.length >0) {
                    let input: Templates.Processor = inputProcessors[0];
                    feed.inputProcessor = input;
                    feed.inputProcessorName = input.name;
                    feed.inputProcessorType = input.type;
                    console.log("set default input processor to be ", input.name)
                }
            }

            this.defineFeedService.saveFeed(feed).subscribe((response:SaveFeedResponse) => {
                if(response.success) {
                    deploy();
                }
            }, error1 =>  {
                //ERROR!!
                this.deployError = true;
                this.deployErrorMessage= "There was an error deploying your feed";
                this.deployingFeed = false;
            })
        }
        else {
            deploy();
        }


    }

    private isUndefined(obj:any){
     return   obj == undefined || obj == null || obj == ""
    }


    /**
     * Cancel this dialog.
     */
    cancel() {
        this.dialog.close();
    }


}
