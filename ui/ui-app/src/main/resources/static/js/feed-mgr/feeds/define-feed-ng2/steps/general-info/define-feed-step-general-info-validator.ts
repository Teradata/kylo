import {FeedStepValidator} from "../../model/feed-step-validator";
import {FeedModel} from "../../model/feed.model";


export class DefineFeedStepGeneralInfoValidator  extends FeedStepValidator {



    public validate(feed:FeedModel) : boolean{
        if(feed.feedName && feed.category) {
            this.step.valid = true;
            this.step.setComplete(true);
        }
        else {
            this.step.valid = false;
            this.step.setComplete(false)
        }
        console.log("Validate step finished",this.step, feed)
        return this.step.complete;
    }
}