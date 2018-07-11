import {FeedStepValidator} from "../../model/feed-step-validator";
import {FeedModel} from "../../model/feed.model";


export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    public validate(feed:FeedModel) : boolean{
        this.step.setComplete(true);
        this.step.valid = true;
        console.log("Validate finished",this.step, feed)
        return true;
    }
}