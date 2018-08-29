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
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    disabled: boolean;
    visited: boolean;
    validator: FeedStepValidator

    validate(feed: Feed): boolean {
        if (this.disabled) {
            return true;
        }
        else {
            return this.validator ? this.validator.validate(feed) : true;
        }

    }

    updateStepState() {
        let disabled = !this.complete;
        //update dependent step states
        let dependentSteps = this.findDependentSteps();
        if (dependentSteps) {
            dependentSteps.forEach(step => step.disabled = disabled);
        }
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




export class StepBuilder {
    number: number;
    systemName: string;
    name: string;
    description: string;
    complete: boolean;
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    disabled: boolean;
    validator: FeedStepValidator;

    setNumber(num: number): StepBuilder {
        this.number = num;
        return this;
    }

    setSystemName(sysName: string) {
        this.systemName = sysName;
        if (this.name == undefined) {
            this.name = this.systemName;
        }
        return this;
    }

    setName(name: string) {
        this.name = name;
        if (this.systemName == undefined) {
            this.systemName = this.name;
        }
        return this;
    }

    setDescription(description: string) {
        this.description = description;
        return this;
    }

    setSref(sref: string) {
        this.sref = sref;
        return this;
    }

    setRequired(required: boolean) {
        this.required = required;
        return this;
    }

    addDependsUpon(systemName: string) {
        this.dependsUponSteps.push(systemName);
        return this;
    }

    setDependsUpon(systemNames: string[]) {
        this.dependsUponSteps = systemNames;
        return this;
    }

    setAllSteps(steps: Step[]) {
        this.allSteps = steps;
        return this;
    }

    setDisabled(disabled: boolean) {
        this.disabled = disabled;
        return this;
    }

    setValidator(feedStepValidator: FeedStepValidator) {
        this.validator = feedStepValidator;
        return this;
    }

    build() {
        let step = new Step({
            number: this.number,
            name: this.name,
            systemName: this.systemName,
            description: this.description,
            sref: FEED_DEFINITION_SECTION_STATE_NAME+"." + this.sref,
            complete: false,
            dependsUponSteps: this.dependsUponSteps,
            required: this.required
        });
        step.allSteps = this.allSteps;
        step.disabled = this.disabled;
        step.validator = this.validator;
        if (step.validator == undefined) {
            //add a default one
            step.validator = new FeedStepValidator();
        }
        if (step.validator) {
            step.validator.setStep(step)
        }
        return step;
    }


}