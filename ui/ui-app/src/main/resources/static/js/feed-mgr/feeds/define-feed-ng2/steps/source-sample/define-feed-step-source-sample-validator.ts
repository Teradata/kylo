import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";

export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    constructor(){
        super();
    }

    public validate(feed:Feed) : boolean {
        if(feed.sourceDataSets && feed.sourceDataSets.length>0){
            this.step.valid = true;
            this.step.setComplete(true)
        }
        else {
            this.step.valid = false;
            this.step.setComplete(false)
        }
        return this.step.valid;
    }
}