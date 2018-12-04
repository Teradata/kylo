import {FeedStepValidator} from "./feed-step-validator";
import {Feed} from "./feed.model";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME} from "./feed-constants";
import {FeedStepRequiredCheck} from "./feed-step-required-check";


export class Step {
    number: number;
    systemName: string;
    name: string;
    description: string;
    /**
     * is the step complete (passed all validation and is ok to be deployed
     */
    complete: boolean;
    /**
     * is the step valid?
     */
    valid: boolean;
    dirty:boolean = false;
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    /**
     * is the step disabled?
     */
    disabled: boolean;
    /**
     * has the user visited this step?
     */
    visited: boolean;
    /**
     * has this feed passed validation and been saved?
     */
    saved:boolean = false;

    icon:string;
    fullscreen:boolean;
    validator: FeedStepValidator

    /**
     * should we completely remove it from view
     */
    hidden:boolean = false;

    feedStepRequiredCheck: FeedStepRequiredCheck;
    /**
     * any additional properties to add and persist for this step
     */
    properties:{ [key: string]: any; } = {};

    public constructor(init?: Partial<Step>) {
        Object.assign(this, init);
        if(!this.properties){
            this.properties = {};
        }
        if(this.feedStepRequiredCheck == undefined){
            this.feedStepRequiredCheck = new FeedStepRequiredCheck();
        }
    }

    validate(feed: Feed): boolean {
        if (this.disabled) {
            return true;
        }
        else {
            if(this.validator){
                return this.validator.validate(feed, this);
            }
            else {
                return true;
            }
        }

    }

    addProperty(key:string,value:any){
        this.properties[key] = value;
    }

    getProperty(key:string){
        return this.properties[key];
    }

    hasProperty(key:string){
        return this.properties && this.properties[key] !== undefined;
    }


    getPropertyAsBoolean(key:string):boolean{
      let p = this.getProperty(key);
      if(p && typeof p == "boolean") {
          return <boolean>p;
      }
      return false;
    }

    markDirty(){
        this.dirty = true;
    }

    isDirty(){
        return this.dirty;
    }

    clearDirty() {
        this.dirty = false;
    }

    /**
     * updates the state of any dependent steps based upon the state of this feed and returns any steps that are disabled due to this feed not being complete
     * @return {Step[]}
     */
    updateStepState() :Step[]{
        let disabled = !this.complete;
        //update dependent step states
        let dependentSteps = this.findDependentSteps();
        if (dependentSteps) {
            dependentSteps.forEach(step => step.disabled = disabled);
        }
        if(disabled) {
           return dependentSteps;
        }
        else {
            return [];
        }
    }

    /**
     * Return the first step that this feed depends upon that is not complete
     * @return {Step | null}
     */
    findFirstIncompleteDependentStep(){
        let dependentSteps = this.findDependsUponSteps();
        if (dependentSteps) {
         let step = dependentSteps.sort((x:Step,y:Step) =>  x.number > y.number ? 1 : 0).filter(step => step != undefined && !step.hidden).find(step =>  step.complete == false);
         return step != undefined ? step : null
        }
        return null;
    }




    setComplete(complete: boolean) {
        this.complete = complete;
    }

    /**
     * return an array of Step objects that this step depends upon
     * @return {(Step | undefined)[]}
     */
    findDependsUponSteps() {
       return this.dependsUponSteps.map(stepName => this.allSteps.find(step => step.systemName == stepName)).filter(step => typeof(step) !== "undefined");
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

    update(step:Step) {
            this.complete = step.complete;
            this.visited = step.visited;
            this.valid = step.valid
            this.saved = step.saved;
            if(step.required != undefined){
                this.required = step.required;
            }
        if(step.properties) {
            this.properties = step.properties;
        }

    }

    /**
     * does this step differ from the incoming step
     * @param {Step} step
     * @return {boolean}
     */
    isStepStateChange(step:Step){
        return (this.systemName == step.systemName && (this.valid != step.valid || this.complete != step.complete || this.visited != step.visited))
    }

    isRequired(feed:Feed){
        return this.feedStepRequiredCheck.isRequired(feed,this);
    }


}
