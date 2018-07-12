import {FeedModel, Step} from "./feed.model";

export class FeedStepValidator  {

    public hasFormErrors:boolean;

    public step : Step;

    constructor(step?:Step) {
    this.step = step;
    }

    public setStep(step:Step){
        this.step = step;
    }

    public validate(feed:FeedModel) : boolean{
        console.log("Validating ",this.step.name)
        this.step.setComplete(true);
        this.step.valid = true;
        return true;
    }

}