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
        console.log("Validating ",this.step.name)
        this.step.setComplete(true);
        this.step.valid = true;
        return this.step.valid;
    }

}