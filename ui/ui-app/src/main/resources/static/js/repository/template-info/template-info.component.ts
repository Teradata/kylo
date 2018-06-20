import {Component, Input, OnInit} from "@angular/core";
import {StateRegistry, StateService} from "@uirouter/angular";
import {RegisterTemplateServiceFactory} from "../../feed-mgr/services/RegisterTemplateServiceFactory";

@Component({
    selector: "template-info",
    templateUrl: "js/repository/template-info/template-info.component.html"
})
export class TemplateInfoComponent implements OnInit{
    public template: any;
    templateId: string;

    constructor(private registerTemplateService: RegisterTemplateServiceFactory, private state: StateService){
        this.template = this.registerTemplateService.model;
        this.templateId = this.state.params.registeredTemplateId;
        console.log("constructor", this.templateId);
    }

    ngOnInit(): void {
        this.templateId = this.state.params.registeredTemplateId;
        console.log("ngInit", this.templateId);
    }


}
