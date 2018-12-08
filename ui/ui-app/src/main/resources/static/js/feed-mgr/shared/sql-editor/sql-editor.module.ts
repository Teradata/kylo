import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
import {FattableModule} from "../fattable/fattable.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from  "@angular/material/select";
import {MatInputModule} from "@angular/material/input";
import {SqlEditorComponent} from "./sql-editor.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {CodemirrorModule} from "ng2-codemirror";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentMessageModule } from '@covalent/core/message';
import {TranslateModule} from "@ngx-translate/core";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {KyloCodeMirrorModule} from "../../../codemirror-require/codemirror.module";

@NgModule({
    declarations: [
      SqlEditorComponent
        ],
    entryComponents:[

    ],
    exports:[
        SqlEditorComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatButtonModule,
        MatInputModule,
        MatSelectModule,
        FattableModule,
        FlexLayoutModule,
        MatProgressBarModule,
        FormsModule,
        KyloCodeMirrorModule,
        CodemirrorModule,
        CovalentDialogsModule,
        CovalentMessageModule,
        TranslateModule
    ],
    providers: [

    ]
})
export class SqlEditorModule {


}
