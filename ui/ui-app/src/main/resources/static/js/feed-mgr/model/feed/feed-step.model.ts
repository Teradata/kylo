import {FeedStepValidator} from "./feed-step-validator";
import {Feed} from "./feed.model";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME} from "./feed-constants";


export class Step {
    number: number;
    systemName: string;
    name: string;
    description: string;
    complete: boolean;
    valid: boolean;
    dirty:boolean = false;
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    disabled: boolean;
    visited: boolean;
    icon:string;
    validator: FeedStepValidator

    validate(feed: Feed): boolean {
        if (this.disabled) {
            return true;
        }
        else {
            return this.validator ? this.validator.validate(feed) : true;
        }

    }

    markDirty(){
        this.dirty = true;
    }

    isDirty(){
        return this.dirty;
    }

    updateStepState() {
        let disabled = !this.complete;
        //update dependent step states
        let dependentSteps = this.findDependentSteps();
        if (dependentSteps) {
            dependentSteps.forEach(step => step.disabled = disabled);
        }
    }

    /**
     * Return the first step that this feed depends upon that is not complete
     * @return {Step | null}
     */
    findFirstIncompleteDependentStep(){
        let dependentSteps = this.findDependsUponSteps();
        if (dependentSteps) {
         let step = dependentSteps.sort((x:Step,y:Step) =>  x.number > y.number ? 1 : 0).find(step => step.complete == false);
         return step != undefined ? step : null
        }
        return null;
    }


    public constructor(init?: Partial<Step>) {
        Object.assign(this, init);
    }

    setComplete(complete: boolean) {
        this.complete = complete;
    }

    isPreviousStepComplete() {
        let index = this.number - 1;
        if (index > 0) {
            let prevStep = this.allSteps[index - 1];
            return prevStep.complete;
        }
        else {
            return true;
        }
    }

    /**
     * return an array of Step objects that this step depends upon
     * @return {(Step | undefined)[]}
     */
    findDependsUponSteps() {
       return  this.dependsUponSteps.map(stepName => this.allSteps.find(step => step.systemName == stepName))
    }

    /**
     * Return an array of objects that depend upon this step
     * @return {Step[]}
     */
    findDependentSteps() {
        return this.allSteps.filter(step => step.dependsUponSteps.find(name => this.systemName == name) != undefined);
    }

    isDisabled() {
        return this.disabled;
    }

    shallowCopy():Step{
        return Object.assign(Object.create(this),this)
    }


}
