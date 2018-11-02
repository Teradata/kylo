import {CommonModule} from "@angular/common";
import {Injector, NgModule} from "@angular/core";
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
import {KyloFeedManagerModule} from "../feed-mgr/feed-mgr.module";
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
import {ImportTemplateComponent, ImportTemplateDirective} from "./ng5-import-template.component";
import {CovalentCommonModule} from "@covalent/core/common";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSortModule} from "@angular/material/sort";
import {MatInputModule} from "@angular/material/input";
import {CovalentNotificationsModule} from "@covalent/core/notifications";
import {CovalentExpansionPanelModule} from "@covalent/core/expansion-panel";
import {TemplateUpdatesDialog} from "./dialog/template-updates-dialog";
import {MatExpansionModule} from "@angular/material/expansion";
import {CovalentVirtualScrollModule} from "@covalent/core/virtual-scroll";
import {MatChipsModule} from "@angular/material/chips";
import {TemplateChangeCommentsComponent} from "./template-change-comments.component";
import {MatIconModule} from "@angular/material/icon";

const moduleName: string = require("../feed-mgr/templates/module-name");

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
        UIRouterModule.forChild({states: repositoryStates})
    ],
    providers:[
        TemplateService
    ],
    entryComponents: [TemplatePublishDialog, TemplateUpdatesDialog]
})
export class RepositoryModule {

    constructor(injector: Injector) {
        // Lazy load AngularJS module and entry component
        require("../feed-mgr/templates/module");
        injector.get("$ocLazyLoad").inject(moduleName);
        require("../feed-mgr/templates/module-require");
        require("../feed-mgr/templates/import-template/ImportTemplateController");
    }
}
