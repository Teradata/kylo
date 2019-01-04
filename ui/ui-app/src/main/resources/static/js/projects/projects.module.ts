import {NgModule} from "@angular/core";
import {ProjectsComponent} from "./projects.component";
import {UIRouterModule} from "@uirouter/angular";
import {projectStates} from "./projects.states";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {ProjectCardComponent} from "./project-card/project-card.component";
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {CommonModule} from "@angular/common";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatDividerModule} from "@angular/material/divider";
import {ProjectDetailsComponent} from "./project-details/project-details.component";

@NgModule({
    declarations: [
        ProjectsComponent,
        ProjectCardComponent,
        ProjectDetailsComponent
    ],
    exports: [],
    imports: [
        CommonModule,
        FlexLayoutModule,
        MatProgressBarModule,
        MatDividerModule,
        MatIconModule,
        MatCardModule,
        UIRouterModule.forChild({states: projectStates})
    ]
})
export class ProjectsModule {
}