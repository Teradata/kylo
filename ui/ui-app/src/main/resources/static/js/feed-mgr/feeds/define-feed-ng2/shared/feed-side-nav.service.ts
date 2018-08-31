import {Step} from "../../../model/feed/feed-step.model";
import {Injectable, TemplateRef} from "@angular/core";
import {FeedLink} from "./feed-link.model";
import {Feed} from "../../../model/feed/feed.model";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";


export class FeedLinkSelectionChangedEvent{
    constructor(public newLink:FeedLink, public oldLink?:FeedLink){}
}
@Injectable()
export class FeedSideNavService {


    /**
     * Allow other components to listen for changes
     *
     */
    public sideNavSelectionChanged$: Observable<FeedLinkSelectionChangedEvent>;


    private sideNavSelectionChangedSubject: Subject<FeedLinkSelectionChangedEvent>;

    constructor(){
        this.sideNavSelectionChangedSubject = new Subject<FeedLinkSelectionChangedEvent>();
        this.sideNavSelectionChanged$ = this.sideNavSelectionChangedSubject.asObservable();
    }

    feedLinks:FeedLink[];

    selectedLink:FeedLink = null;

    toolbarActionTemplateRefMap:{ [key: string]: TemplateRef<any> } = {}

    registerToolbarActionTemplate(linkName:string,templateRef:TemplateRef<any>){
        let link = this._findLinkByName(linkName);
        if(link){
            this.toolbarActionTemplateRefMap[linkName] = templateRef;
            //fire link changed event?
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
        let stepLink = this.feedLinks.find((link) => link.isStepLink() && link.step.name == step.name);
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