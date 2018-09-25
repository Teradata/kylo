import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";


export class DefineFeedPropertiesValidator  extends FeedStepValidator {



    public validate(feed:Feed, step:Step) : boolean{
        let valid = super.validate(feed,step);

      /*  if(valid && feed.userProperties.length){
            step.setComplete(true);
        }
        else {
            step.setComplete(false);
        }
        */
        return valid;
    }
}