import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatListModule} from "@angular/material/list";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentSearchModule} from "@covalent/core/search";
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../common/common.module";
import {RepositoryComponent} from "./repository.component";
import {repositoryStates} from "./repository.states";
import {ListTemplatesComponent} from "./list/list.component";
import {TemplateService} from "./services/template.service";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {CovalentPagingModule} from "@covalent/core/paging";
import {MatSelectModule} from "@angular/material/select";
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatTableModule} from "@angular/material/table";
import {CdkTableModule} from "@angular/cdk/table";
import {TemplateInfoComponent} from "./template-info/template-info.component";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {TemplatePublishDialog} from "./dialog/template-publish-dialog";
import {MatRadioModule} from "@angular/material/radio";

@NgModule({
    declarations: [
        ListTemplatesComponent,
        RepositoryComponent,
        TemplateInfoComponent,
        TemplatePublishDialog
    ],
    imports: [
        FormsModule,
        CommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentPagingModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatCardModule,
        MatDividerModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        MatCheckboxModule,
        MatSelectModule,
        MatButtonModule,
        MatTableModule,
        MatProgressSpinnerModule,
        CdkTableModule,
        MatMenuModule,
        MatProgressBarModule,
        MatRadioModule,
        UIRouterModule.forChild({states: repositoryStates})
    ],
    providers:[
        TemplateService
    ],
    entryComponents: [TemplatePublishDialog]
})
export class RepositoryModule {
}
