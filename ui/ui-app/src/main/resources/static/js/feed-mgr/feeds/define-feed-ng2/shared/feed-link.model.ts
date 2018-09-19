import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../model/feed/feed-constants";

export enum FeedLinkType {
    STEP=1, SUMMARY=2, LINK=3
}
export class FeedLink{
    step?:Step;
    sref:string;
    icon?:string;
    selected:boolean;
    srefParams?:any;
    type:FeedLinkType = FeedLinkType.LINK;

    constructor(public label:string) {

    }

    static emptyLink(){
        return new FeedLink("");
    }


    static newStaticLink(label:string,sref:string, icon?:string, params?:any):FeedLink{
        let link = new FeedLink(label);
        link.setSRef(FEED_DEFINITION_SUMMARY_STATE_NAME+"."+sref,false)
        link.setIcon(icon);
        link.type = FeedLinkType.LINK
        link.srefParams = params;
        return link;
    }

    static newSectionLink(label:string,sref:string, icon?:string,params?:any):FeedLink{
        let link = new FeedLink(label);
        link.setSRef(FEED_DEFINITION_SECTION_STATE_NAME+"."+sref,false)
        link.setIcon(icon);
        link.type = FeedLinkType.LINK
        link.srefParams = params;
        return link;
    }



    static newStepLink(step:Step):FeedLink{
        let link = new FeedLink(step.name);
        link.sref =step.sref;
        link.step = step;
        if(step.icon){
            link.icon = step.icon;
        }
        link.type = FeedLinkType.STEP
        return link;
    }

    static newSummaryLink(label:string,shortSRef:string, icon?:string):FeedLink{
        let link = new FeedLink(label);
        link.setSRef(shortSRef,true)
        link.setIcon(icon);
        link.type = FeedLinkType.SUMMARY
        return link;
    }

    setIcon(icon:string)
    {
        this.icon = icon;
    }
    setSRef(sref:string, isShort:boolean = true){
        if(isShort) {
            this.sref = FEED_DEFINITION_SECTION_STATE_NAME+"."+sref;
        }
        else {
            this.sref = sref;
        }
    }

    setStep(step:Step){
        this.step = step;
    }

    isStepLink(){
        return this.type == FeedLinkType.STEP
    }

    isFeedLink(){
        return this.type == FeedLinkType.LINK
    }

    isSummaryLink(){
        return this.type == FeedLinkType.SUMMARY
    }

}
