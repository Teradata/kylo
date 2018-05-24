import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UIRouterModule} from "@uirouter/angular";

import {PreviewSchemaComponent} from "./preview-schema.component";
import {CovalentDataTableModule} from '@covalent/core/data-table';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';

@NgModule({
    declarations: [
        PreviewSchemaComponent
    ],
    entryComponents: [
        PreviewSchemaComponent
    ],
    imports: [
        CommonModule,
        CovalentDataTableModule,
        MatTabsModule,
        MatSelectModule,
        MatSlideToggleModule,
        UIRouterModule.forChild({
            states: [
                {
                    name: "catalog.datasource.preview",
                    url: "/preview",
                    component: PreviewSchemaComponent
                }
            ]
        })
    ]
})
export class PreviewSchemaModule {
}
