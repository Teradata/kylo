import {Step} from "../../../model/feed/feed-step.model";
import {Injectable, TemplateRef} from "@angular/core";
import {FeedLink} from "./feed-link.model";
import {Feed, LoadMode} from "../../../model/feed/feed.model";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";
import {StateRegistry, StateService} from "@uirouter/angular";
import {TranslateService} from "@ngx-translate/core";
import {LINEAGE_LINK, PROFILE_LINK, SETUP_GUIDE_LINK, SETUP_REVIEW_LINK, SLA_LINK, VERSIONS_LINK} from "./feed-link-constants";
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

    latestSetupGuideLink = FeedLink.newStaticLink(SETUP_GUIDE_LINK, "setup-guide",KyloIcons.Links.setupGuide,{"loadMode":LoadMode.LATEST});

    deployedSetupGuideLink = FeedLink.newStaticLink(SETUP_REVIEW_LINK, "setup-guide",KyloIcons.Links.setupGuide, {"loadMode":LoadMode.DEPLOYED});



    constructor(private stateService:StateService, private _translateService: TranslateService){
        this.sideNavSelectionChangedSubject = new Subject<FeedLinkSelectionChangedEvent>();
        this.sideNavSelectionChanged$ = this.sideNavSelectionChangedSubject.asObservable();

        this.toolbarActionTemplateChangedSubject = new Subject<ToolbarActionTemplateChangedEvent>();
        this.staticFeedLinks = [FeedLink.newStaticLink(LINEAGE_LINK,'feed-lineage',KyloIcons.Links.lineage),
            FeedLink.newStaticLink(PROFILE_LINK,"profile",KyloIcons.Links.profile),
            FeedLink.newStaticLink(SLA_LINK,"sla",KyloIcons.Links.sla),
            FeedLink.newStaticLink(VERSIONS_LINK,"version-history",KyloIcons.Links.versions)];
    }

    feedLinks:FeedLink[] = [];

    selectedLink:FeedLink = null;

    toolbarActionTemplateRefMap:{ [key: string]: TemplateRef<any> } = {}


    subscribeToToolbarActionTemplateChanges(o:PartialObserver<ToolbarActionTemplateChangedEvent>){
        return this.toolbarActionTemplateChangedSubject.subscribe(o);
    }

    registerToolbarActionTemplate(linkName:string,templateRef:TemplateRef<any>){
        let link = this._findLinkByName(linkName);
        if(link){
            this.toolbarActionTemplateRefMap[linkName] = templateRef;
            //fire link changed event?
            console.log("LINK CHANGED!!!!", templateRef)
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

    selectLinkByName(linkName:string){
        let link = this._findLinkByName(linkName);
        if(link){
            this.setSelected(link);
        }
    }

    private _findStepLink(step:Step):FeedLink{
        return this.feedLinks.find((link) => link.isStepLink() && link.step.name == step.name);
    }

    private _findLinkByName(linkName:string){
        return this.feedLinks.find((link) => link.label == linkName);
    }

    subscribeToFeedLinkSelectionChanges(observer:PartialObserver<FeedLinkSelectionChangedEvent>) : ISubscription{
        return this.sideNavSelectionChanged$.subscribe(observer)
    }

    setSelected(feedLink:FeedLink){
        //unselect previous one
        let selectedLink = this.feedLinks.find((link) => link.selected);
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

    buildStepLinks(feed:Feed):FeedLink[]{
        return feed.steps.map((step:Step) => FeedLink.newStepLink(step))
    }

    registerFeedLinks(feedLinks:FeedLink[]){
        this.selectedLink = null;
        this.feedLinks = feedLinks;
        let selectedLink = this.feedLinks.find((link) => link.selected);
        if(selectedLink){
            this.selectedLink = selectedLink;
        }

    }
}