import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME,FEED_DEFINITION_SECTION_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedLink} from "../steps/define-feed-container/define-feed-container.component";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Input, Component} from "@angular/core";
import {Feed} from "../../../model/feed/feed.model";
import {DefineFeedService} from "../services/define-feed.service";

@Component({
    selector: "feed-definition-side-nav",
    styleUrls:["js/feed-mgr/feeds/define-feed-ng2/shared/feed-side-nav.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/shared/feed-side-nav.component.html"
})
export class FeedSideNavComponent {
    @Input()
    feed:Feed

    summarySelected:boolean;

    public selectedStep : Step;

    feedLinks:FeedLink[] = [new FeedLink("Lineage",'feed-lineage',"graphic_eq"),new FeedLink("Profile","profile","track_changes"),new FeedLink("SLA","sla","beenhere"),new FeedLink("Versions","version-history","history")];

    constructor(private  stateService:StateService, private defineFeedService:DefineFeedService) {
        this.summarySelected = true;
        this.defineFeedService.subscribeToStepChanges(this.onStepChanged.bind(this))
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

        let selected = currSref == step.sref && this.selectedStep != undefined && step.number == this.selectedStep.number && this.isStepState()

        return selected;

    }
    isSelected(state:string){
        return this.stateService.$current.name == state;
    }

    isSummarySelected(){
    return this.summarySelected;
    }

    onLinkSelected(link:FeedLink){
        this.stateService.go(link.sref,{"feedId":this.feed.id})
        this.summarySelected = false;
        this.selectedStep = undefined;
    }

    onStepSelected(step:Step){
        if(!step.isDisabled()) {
            this.selectedStep = step;
            this.stateService.go(step.sref,{"feedId":this.feed.id})
            this.summarySelected = false;
        }
    }

    /**
     * Listen when the step changes
     * @param {Step} step
     */
    onStepChanged(step:Step){
        this.selectedStep = step;
        this.summarySelected = false;
    }

    onSummarySelected(){
        this.summarySelected = true;
        this.selectedStep = undefined;
        this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".overview",{"feedId":this.feed.id})
    }


}