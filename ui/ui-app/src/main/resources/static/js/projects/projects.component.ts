import {Component} from "@angular/core";
import {AddButtonService} from "../services/AddButtonService";
import {StateService} from "@uirouter/core";

@Component({
    selector: "list-projects",
    templateUrl: "./projects.component.html"
})
export class ProjectsComponent {
    constructor(private addButtonService: AddButtonService, private state: StateService){
        this.addButtonService.registerAddButton('projects', ()=> {
            this.state.go("project-details");
        });
    }
}