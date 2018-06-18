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

@NgModule({
    declarations: [
        ListTemplatesComponent,
        RepositoryComponent
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
        UIRouterModule.forChild({states: repositoryStates})
    ],
    providers:[
        TemplateService
    ]
})
export class RepositoryModule {
}
