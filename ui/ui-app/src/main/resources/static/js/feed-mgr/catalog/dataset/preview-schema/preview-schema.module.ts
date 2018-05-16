import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";

import {PreviewSchemaComponent} from "./preview-schema.component";

@NgModule({
    declarations: [
        PreviewSchemaComponent
    ],
    entryComponents: [
        PreviewSchemaComponent
    ],
    imports: [
        CommonModule,
        UIRouterModule.forChild({
            states: [
                {
                    name: "explorer.dataset.preview",
                    url: "/preview",
                    component: PreviewSchemaComponent
                }
            ]
        })
    ]
})
export class PreviewSchemaModule {
}
