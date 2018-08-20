import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";


export class DefineFeedStepGeneralInfoValidator  extends FeedStepValidator {



    public validate(feed:Feed) : boolean{
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
        return this.step.valid;
    }
}