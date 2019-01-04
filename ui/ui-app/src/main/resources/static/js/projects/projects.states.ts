import {Ng2StateDeclaration} from "@uirouter/angular";
import {ProjectsComponent} from "./projects.component";
import {ProjectDetailsComponent} from "./project-details/project-details.component";

export const projectStates: Ng2StateDeclaration[] = [
    {
        name: "projects",
        url: "/projects",
        views: {
            "content": {
                component: ProjectsComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Projects",
        }
    },
    {
        name: "project-details",
        url: "/project-details",
        views: {
            "content": {
                component: ProjectDetailsComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Project Details",
        }
    }
];