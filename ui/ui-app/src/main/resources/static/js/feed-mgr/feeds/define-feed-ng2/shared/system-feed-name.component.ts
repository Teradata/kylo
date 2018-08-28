import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Component, Inject, Input, OnDestroy, OnInit} from "@angular/core";
import {Feed} from "../../../model/feed/feed.model";
import {FeedService} from "../../../services/FeedService";

@Component({
    selector:"system-feed-name",
    templateUrl:"js/feed-mgr/feeds/define-feed-ng2/shared/system-feed-name.component.html"
})
export class SystemFeedNameComponent implements OnInit, OnDestroy{

    @Input()
    formGroup:FormGroup

    @Input()
    feed?:Feed;


    constructor(@Inject("FeedService") private feedService:FeedService) {


    }

    private generateSystemName(value:string) {
        this.feedService.getSystemName(value).then((response: any) => {
            this.formGroup.get("systemFeedName").setValue(response.data);
        });
    }



    ngOnInit(){
        let feedName = this.feed != undefined ? this.feed.feedName : '';
        let systemFeedName = this.feed != undefined ? this.feed.systemFeedName : '';


        let feedNameCtrl = new FormControl(feedName,[Validators.required])
        feedNameCtrl.valueChanges.debounceTime(200).subscribe(value => {
            if(this.formGroup.get("systemFeedName").untouched) {
                this.generateSystemName(value);
            }
            else {
                console.log('system name will not auto generate.  its been touched')
            }
        });
        this.formGroup.registerControl("feedName",feedNameCtrl);

        //TODO add in pattern validator, and unique systemFeedName validator
        this.formGroup.addControl("systemFeedName", new FormControl(systemFeedName,[Validators.required]))
    }

    ngOnDestroy(){

    }
}