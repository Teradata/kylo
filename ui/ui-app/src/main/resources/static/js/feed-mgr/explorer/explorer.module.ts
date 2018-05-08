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

import {KyloCommonModule} from "../../common/common.module";
import {CatalogModule} from "./catalog/catalog.module";
import {ConnectorsComponent} from "./connectors/connectors.component";
import {DatasetComponent} from "./dataset/dataset.component";
import {ExplorerComponent} from "./explorer.component";
import {explorerStates} from "./explorer.states";

@NgModule({
    declarations: [
        ConnectorsComponent,
        DatasetComponent,
        ExplorerComponent,
    ],
    imports: [
        CatalogModule,
        CommonModule,
        CovalentDataTableModule,
        CovalentDialogsModule,
        CovalentLayoutModule,
        CovalentLoadingModule,
        CovalentSearchModule,
        FlexLayoutModule,
        KyloCommonModule,
        MatCardModule,
        MatDividerModule,
        MatListModule,
        MatTabsModule,
        MatToolbarModule,
        UIRouterModule.forChild({states: explorerStates})
    ]
})
export class ExplorerModule {
}
