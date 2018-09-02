import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";


export class DefineFeedPropertiesValidator  extends FeedStepValidator {



    public validate(feed:Feed) : boolean{
        let valid = super.validate(feed);

        if(valid && feed.userProperties.length){
            this.step.setComplete(true);
        }
        else {
            this.step.setComplete(false);
        }
        return valid;
    }
}