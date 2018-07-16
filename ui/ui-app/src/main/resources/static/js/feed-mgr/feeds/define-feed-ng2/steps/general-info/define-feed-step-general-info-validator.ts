import {FeedStepValidator} from "../../model/feed-step-validator";
import {FeedModel} from "../../model/feed.model";


export class DefineFeedStepGeneralInfoValidator  extends FeedStepValidator {



    public validate(feed:FeedModel) : boolean{
        if(this.hasFormErrors){
            this.step.setComplete(false);
        }
        else if(feed.feedName && feed.category && feed.category.id) {
            this.step.valid = true;
            this.step.setComplete(true);
        }
        else {
            this.step.valid = false;
            this.step.setComplete(false)
        }
        console.log("Validate step finished",this.step, feed)
        return this.step.valid;
    }
}