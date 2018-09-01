import {Step} from "./feed-step.model";
import {FeedStepValidator} from "./feed-step-validator";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "./feed-constants";

export class FeedStepBuilder {

    steps: StepBuilder[];

    constructor() {
        this.steps = [];
    }

    addStep(step:StepBuilder){
        this.steps.push(step);
        return this;
    }


    build() :Step[]{
        let allSteps:Step[] = [];
        return this.steps.map(step => {
                step.setAllSteps(allSteps);
                return step.build();
            }
        );
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
    icon:string;
    validator: FeedStepValidator;
    feedStepBuilder?:FeedStepBuilder

    constructor(){
    }

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

    setIcon(icon: string) {
        this.icon = icon;
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
        if(this.number == undefined){
            let num = this.allSteps.length+1;
            this.number = num;
        }
        let step = new Step({
            number: this.number,
            name: this.name,
            systemName: this.systemName,
            description: this.description,
            sref: FEED_DEFINITION_SECTION_STATE_NAME+"." + this.sref,
            complete: false,
            dependsUponSteps: this.dependsUponSteps,
            required: this.required,
            icon:this.icon
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
        if(step.name == undefined){
            step.name = step.systemName;
        }
        this.allSteps.push(step)
        return step;
    }


}