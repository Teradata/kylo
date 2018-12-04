import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {KyloFeedModule, ProcessorControl} from "@kylo/feed";

import {GetFileComponent} from "./get-file.component";

@NgModule({
    declarations: [
        GetFileComponent
    ],
    entryComponents: [
        GetFileComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        KyloFeedModule,
        MatIconModule,
        MatInputModule,
        MatSnackBarModule
    ],
    providers: [
        {provide: ProcessorControl, useValue: new ProcessorControl(GetFileComponent), multi: true}
    ]
})
export class GetFileModule {
}
