import {CdkTableModule} from "@angular/cdk/table";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDividerModule} from "@angular/material/divider";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {MatTabsModule} from "@angular/material/tabs";
import {MatToolbarModule} from "@angular/material/toolbar";
import {CovalentCommonModule} from "@covalent/core/common";
import {CovalentDataTableModule} from "@covalent/core/data-table";
import {CovalentDialogsModule} from "@covalent/core/dialogs";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentPagingModule} from "@covalent/core/paging";
import {CovalentSearchModule} from "@covalent/core/search";
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../common/common.module";
import {TemplatePublishDialog} from "./dialog/template-publish-dialog";
import {ListTemplatesComponent} from "./list/list.component";
import {ImportTemplateComponent, ImportTemplateDirective} from "./ng5-import-template.component";
import {RepositoryComponent} from "./repository.component";
import {repositoryStates} from "./repository.states";
import {TemplateInfoComponent} from "./template-info/template-info.component";
import { TemplateService } from "../services/template.service";

@NgModule({
    declarations: [
        ListTemplatesComponent,
        RepositoryComponent,
        TemplateInfoComponent,
        TemplatePublishDialog,
        ImportTemplateComponent,
        ImportTemplateDirective
    ],
    imports: [
        FormsModule,
        CommonModule,
        CovalentCommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        CovalentPagingModule,
        CovalentNotificationsModule,
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
        MatPaginatorModule,
        MatSortModule,
        MatInputModule,
        MatProgressSpinnerModule,
        CdkTableModule,
        MatMenuModule,
        MatProgressBarModule,
        MatRadioModule,
        UIRouterModule.forChild({states: repositoryStates})
    ],
    providers: [
        TemplateService
    ],
    entryComponents: [TemplatePublishDialog]
})
export class RepositoryModule {
}
