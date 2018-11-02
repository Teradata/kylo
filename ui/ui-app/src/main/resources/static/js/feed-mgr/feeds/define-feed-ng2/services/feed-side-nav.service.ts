import {Step} from "../../../model/feed/feed-step.model";
import {Injectable, TemplateRef} from "@angular/core";
import {FeedLink, FeedLinkType} from "../model/feed-link.model";
import {Feed, LoadMode} from "../../../model/feed/feed.model";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";
import {StateRegistry, StateService} from "@uirouter/angular";
import {TranslateService} from "@ngx-translate/core";
import {FEED_ACTIVITY_LINK, LINEAGE_LINK, PROFILE_LINK, SETUP_GUIDE_LINK, SETUP_REVIEW_LINK, SLA_LINK, VERSIONS_LINK} from "../model/feed-link-constants";
import {KyloIcons} from "../../../../kylo-utils/kylo-icons";


export class FeedLinkSelectionChangedEvent{
    constructor(public newLink:FeedLink, public oldLink?:FeedLink){}
}

export class ToolbarActionTemplateChangedEvent{
    constructor(public link:FeedLink,public templateRef:TemplateRef<any>){}
}

@Injectable()
export class FeedSideNavService {

    /**
     * Allow other components to listen for changes
     *
     */
    public sideNavSelectionChanged$: Observable<FeedLinkSelectionChangedEvent>;

    private sideNavSelectionChangedSubject: Subject<FeedLinkSelectionChangedEvent>;

    public toolbarActionTemplateChanged$: Observable<ToolbarActionTemplateChangedEvent>;

    private toolbarActionTemplateChangedSubject: Subject<ToolbarActionTemplateChangedEvent>;

    /**
     * Static links
     * @type {FeedLink[]}
     */
    public staticFeedLinks:FeedLink[] =[];

    /**
     * the current step links
     * @type {any[]}
     */
    stepLinks:FeedLink[] = [];

    /**
     * all the links
     */
    allLinks:FeedLink[] = [];

    selectedLink:FeedLink = null;

    summaryLinkLatestSetupGuideLink = FeedLink.newStaticLink(SETUP_GUIDE_LINK, "setup-guide",KyloIcons.Links.setupGuide,{"loadMode":LoadMode.LATEST});

    summaryLinkDeployedSetupGuideLink = FeedLink.newStaticLink(SETUP_REVIEW_LINK, "setup-guide",KyloIcons.Links.setupGuide, {"loadMode":LoadMode.DEPLOYED});

    feedActivityLink = FeedLink.newStaticLink(FEED_ACTIVITY_LINK, "feed-activity","dashboard");



    sectionLinkDeployedSetupGuideLink = FeedLink.newSectionLink(SETUP_REVIEW_LINK, "setup-guide",KyloIcons.Links.setupGuide, {"loadMode":LoadMode.DEPLOYED});

    sectionLinkSetupGuideSummaryLink = FeedLink.newSectionLink(SETUP_GUIDE_LINK, "setup-guide",KyloIcons.Links.setupGuide);


    constructor(private stateService:StateService, private _translateService: TranslateService){
        this.sideNavSelectionChangedSubject = new Subject<FeedLinkSelectionChangedEvent>();
        this.sideNavSelectionChanged$ = this.sideNavSelectionChangedSubject.asObservable();
        this.toolbarActionTemplateChangedSubject = new Subject<ToolbarActionTemplateChangedEvent>();
        this.buildSummaryLinks();
        this.buildStaticLinks();


    }

    buildStaticLinks(){
        this.staticFeedLinks = [
            FeedLink.newStaticLink(LINEAGE_LINK,'feed-lineage',KyloIcons.Links.lineage),
            FeedLink.newStaticLink(PROFILE_LINK,"profile",KyloIcons.Links.profile),
            FeedLink.newStaticLink(SLA_LINK,"sla",KyloIcons.Links.sla),
            FeedLink.newStaticLink(VERSIONS_LINK,"version-history",KyloIcons.Links.versions)];
        this.staticFeedLinks.forEach(link => this.allLinks.push(link));
    }

    buildSummaryLinks(){
        this.allLinks.push(this.summaryLinkLatestSetupGuideLink);
        this.allLinks.push(this.summaryLinkDeployedSetupGuideLink);
        this.allLinks.push(this.feedActivityLink);
        this.allLinks.push(this.sectionLinkSetupGuideSummaryLink)
        this.allLinks.push(this.sectionLinkDeployedSetupGuideLink)
    }



    toolbarActionTemplateRefMap:{ [key: string]: TemplateRef<any> } = {}


    subscribeToToolbarActionTemplateChanges(o:PartialObserver<ToolbarActionTemplateChangedEvent>){
        return this.toolbarActionTemplateChangedSubject.subscribe(o);
    }

    registerStepToolbarActionTemplate(linkName:string,templateRef:TemplateRef<any>){
        let link = this._findStepLinkByName(linkName);
        if(!link){
            link = this._findSetupGuideLink(linkName);
        }
        if(link){
            this.toolbarActionTemplateRefMap[linkName] = templateRef;
            //fire link changed event
            this.toolbarActionTemplateChangedSubject.next(new ToolbarActionTemplateChangedEvent(link,templateRef))
       }
    }

    getSelectedLinkTemplateRef(){
        if(this.selectedLink) {
            return this.toolbarActionTemplateRefMap[this.selectedLink.label];
        }
    }

    getLinkTemplateRef(linkName:string){
        return this.toolbarActionTemplateRefMap[linkName];
    }

    setStepSelected(step:Step){
        let stepLink = this._findStepLink(step);
        if(stepLink){
            this.setSelected(stepLink);
        }
    }

    selectStaticLinkByName(linkName:string){
        let link = this._findStaticLinkByName(linkName);
        if(link){
            this.setSelected(link);
        }
    }

    private _findStepLink(step:Step):FeedLink{
        return this.allLinks.find((link) => link.isStepLink() && link.step.name == step.name);
    }

    private _findStaticLinkByName(linkName:string){
        return this.allLinks.find((link) => link.label == linkName && link.linkType == FeedLinkType.STATIC);
    }

    private _findSetupGuideLink(linkName:string){
        return this.allLinks.find((link) => link.label == linkName && link.linkType == FeedLinkType.SETUP_GUIDE);
    }

    private _findStepLinkByName(linkName:string){
        return this.allLinks.find((link) => link.label == linkName && link.isStepLink());
    }

    subscribeToFeedLinkSelectionChanges(observer:PartialObserver<FeedLinkSelectionChangedEvent>) : ISubscription{
        return this.sideNavSelectionChanged$.subscribe(observer)
    }

    setSelected(feedLink:FeedLink){
        //unselect previous one
        let selectedLink = this.allLinks.find((link) => link.selected);
        if(selectedLink){
            selectedLink.selected = false;
        }
        feedLink.selected = true;
        if(this.selectedLink == undefined || this.selectedLink.label != feedLink.label) {
            this.sideNavSelectionChangedSubject.next(new FeedLinkSelectionChangedEvent(feedLink,selectedLink))
        }
        this.selectedLink = feedLink;

    }

    gotoStep(step:Step,feedId:string){
        let link = this._findStepLink(step);
        if(link){
            this.stateService.go(link.sref,{feedId:feedId})
        }
    }

    removeLink(linkToRemove:FeedLink){
        let idx = this.allLinks.findIndex(link => link ===linkToRemove);
        if(idx >=0){
            this.allLinks.splice(idx,1);
        }
    }

    buildStepLinks(feed:Feed):FeedLink[]{
        //remove old step links
        let stepLinks = this.allLinks.filter(link => link.isStepLink());
        stepLinks.forEach(link => this.removeLink(link));

         this.stepLinks = feed.steps.filter(step=> !step.hidden).map((step:Step) => {
           let link = FeedLink.newStepLink(step)
            this.allLinks.push(link);
           return link;
        });
         return this.stepLinks;
    }


}