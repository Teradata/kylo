import {FeedStepValidator} from "../../model/feed-step-validator";
import {FeedModel} from "../../model/feed.model";


export class DefineFeedStepSourceSampleValidator extends FeedStepValidator {

    public validate(feed:FeedModel) : boolean {
        if (feed.table.sourceTableSchema.fields.length == 0) {
            this.step.valid = true;
        }
        else {
            this.step.setComplete(true);
            this.step.valid = true;
        }
        return this.step.valid;
    }
}