import {CdkTableModule} from "@angular/cdk/table";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatChipsModule} from "@angular/material/chips";
import {MatDividerModule} from "@angular/material/divider";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatIconModule} from "@angular/material/icon";
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
import {CovalentExpansionPanelModule} from "@covalent/core/expansion-panel";
import {CovalentLayoutModule} from "@covalent/core/layout";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentPagingModule} from "@covalent/core/paging";
import {CovalentSearchModule} from "@covalent/core/search";
import {CovalentVirtualScrollModule} from "@covalent/core/virtual-scroll";
import {TranslateModule} from "@ngx-translate/core";
import {UIRouterModule} from "@uirouter/angular";

import {KyloCommonModule} from "../common/common.module";
import {KyloFeedManagerModule} from "../feed-mgr/feed-mgr.module";
import {TemplateService} from "../services/template.service";
import {TemplatePublishDialog} from "./dialog/template-publish-dialog";
import {TemplateUpdatesDialog} from "./dialog/template-updates-dialog";
import {ListTemplatesComponent} from "./list/list.component";
import {ImportTemplateComponent, ImportTemplateDirective} from "./ng5-import-template.component";
import {RepositoryComponent} from "./repository.component";
import {repositoryStates} from "./repository.states";
import {TemplateChangeCommentsComponent} from "./template-change-comments.component";
import {TemplateInfoComponent} from "./template-info/template-info.component";

@NgModule({
    declarations: [
        ListTemplatesComponent,
        RepositoryComponent,
        TemplateInfoComponent,
        TemplateChangeCommentsComponent,
        TemplatePublishDialog,
        TemplateUpdatesDialog,
        ImportTemplateComponent,
        ImportTemplateDirective
    ],
    imports: [
        CdkTableModule,
        CommonModule,
        CovalentCommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentExpansionPanelModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentNotificationsModule,
        CovalentPagingModule,
        CovalentSearchModule,
        CovalentVirtualScrollModule,
        FlexLayoutModule,
        FormsModule,
        KyloCommonModule,
        KyloFeedManagerModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatChipsModule,
        MatDividerModule,
        MatExpansionModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatMenuModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatRadioModule,
        MatSelectModule,
        MatSortModule,
        MatTableModule,
        MatTabsModule,
        MatToolbarModule,
        UIRouterModule.forChild({states: repositoryStates}),
        TranslateModule
    ],
    providers: [
        TemplateService
    ],
    entryComponents: [TemplatePublishDialog, TemplateUpdatesDialog]
})
export class RepositoryModule {
}
