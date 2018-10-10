import {Feed} from "./feed.model";
import {Step} from "./feed-step.model";

export class FeedStepValidator  {

    hasFormErrors:boolean;
    constructor() {
    }


    public validate(feed:Feed, step:Step) : boolean{
        if(step.isRequired(feed) && !step.visited) {
            step.valid = true;
            step.setComplete(false);
        } else if(this.hasFormErrors){
            step.setComplete(false);
            step.valid = false;
        }
        else {
            step.valid = true;
            let complete = step.isRequired(feed) && !step.saved ? false : true
            step.setComplete(complete)
        }
        return step.valid;
    }



}