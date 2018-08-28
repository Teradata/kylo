import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedLink} from "../steps/define-feed-container/define-feed-container.component";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, Component} from "@angular/core";
import {Feed} from "../../../model/feed/feed.model";

@Component({
    selector: "feed-definition-side-nav",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/shared/feed-side-nav.component.html"
})
export class FeedSideNavComponent {
    @Input()
    feed:Feed

    public selectedStep : Step;

    feedLinks:FeedLink[] = [new FeedLink("Lineage",'feed-lineage',"graphic_eq",),new FeedLink("Profile","profile","track_changes"),new FeedLink("SLA","sla","beenhere"),new FeedLink("Versions","version-history","history")];

constructor(private  stateService:StateService) {

}


    gotoFeeds(){
        this.stateService.go("feeds");
    }

    isStepState(){
        let currStep = this.stateService.$current.name;
        return this.feedLinks.find(link=> link.sref == currStep) == undefined;
    }

    isLinkState(){
        return !this.isStepState();
    }

    isSelectedStep(step?:Step){
        let stepSref = step.sref;
        let currSref = this.stateService.$current.name;
        if(currSref == "datasource"){
            currSref = "datasources"
        }

        return currSref == step.sref && this.selectedStep != undefined && step.number == this.selectedStep.number

    }
    isSelected(state:string){
        return this.stateService.$current.name == state;
    }

    onLinkSelected(link:FeedLink){
        this.stateService.go(link.sref,{"feedId":this.feed.id})
    }

    onStepSelected(step:Step){
        if(!step.isDisabled()) {
            this.selectedStep = step;
            this.stateService.go(step.sref,{"feedId":this.feed.id})
        }
    }

    onSummarySelected(){
        this.stateService.go(FEED_DEFINITION_STATE_NAME+".summary",{"feedId":this.feed.id})
    }


}