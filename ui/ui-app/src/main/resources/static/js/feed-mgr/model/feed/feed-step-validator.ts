import {Feed} from "./feed.model";
import {Step} from "./feed-step.model";

export class FeedStepValidator  {

    public hasFormErrors:boolean;

    public step : Step;

    constructor() {
    }

    public setStep(step:Step){
        this.step = step;
    }

    public validate(feed:Feed) : boolean{
        if(this.step.required && !this.step.visited) {
            this.step.setComplete(false);
        } else if(this.hasFormErrors){
            this.step.setComplete(false);
            this.step.valid = false;
        }
        else {
            this.step.valid = true;
            this.step.setComplete(true)
        }
        return this.step.valid;
    }



}