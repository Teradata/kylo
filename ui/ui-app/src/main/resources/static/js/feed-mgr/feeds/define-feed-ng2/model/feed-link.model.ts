import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from "../../../model/feed/feed-constants";

/**
 * describes where the link is
 */
export enum FeedLinkType{
    SETUP_GUIDE="SETUP_GUIDE",STATIC="STATIC"
}

export class FeedLink{
    step?:Step;
    sref:string;
    icon?:string;
    selected:boolean;
    srefParams?:any;
    linkType?:FeedLinkType;

    constructor(public label:string) {

    }

    static emptyLink(){
        return new FeedLink("");
    }

    isSetupGuide(){
        return this.sref.indexOf("setup-guide") >=0;
    }


    /**
     * New Link for the Main page
     * @param {string} label
     * @param {string} sref
     * @param {string} icon
     * @param params
     * @return {FeedLink}
     */
    static newStaticLink(label:string,sref:string, icon?:string, params?:any):FeedLink{
        let link = new FeedLink(label);
        link.setSRef(FEED_DEFINITION_SUMMARY_STATE_NAME+"."+sref)
        link.setIcon(icon);
        link.linkType = FeedLinkType.STATIC;
        link.srefParams = params;
        return link;
    }

    /**
     * New Link for the step sections
     * @param {string} label
     * @param {string} sref
     * @param {string} icon
     * @param params
     * @return {FeedLink}
     */
    static newSectionLink(label:string,sref:string, icon?:string,params?:any):FeedLink{
        let link = new FeedLink(label);
        link.setSRef(FEED_DEFINITION_SECTION_STATE_NAME+"."+sref)
        link.setIcon(icon);
        link.linkType = FeedLinkType.SETUP_GUIDE;
        link.srefParams = params;
        return link;
    }



    static newStepLink(step:Step):FeedLink{
        let link = new FeedLink(step.name);
        link.sref =step.sref;
        link.step = step;
        link.linkType = FeedLinkType.SETUP_GUIDE;
        if(step.icon){
            link.icon = step.icon;
        }
        return link;
    }

    setIcon(icon:string)
    {
        this.icon = icon;
    }
    setSRef(sref:string){
       this.sref = sref;
    }

    setStep(step:Step){
        this.step = step;
    }

    isStepLink(){
        return this.step !== undefined;
    }

}
