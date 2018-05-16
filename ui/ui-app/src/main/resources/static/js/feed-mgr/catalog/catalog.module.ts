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
import {CatalogApiModule} from "./api/catalog-api.module";
import {ConnectorsComponent} from "./connectors/connectors.component";
import {DatasetComponent} from "./dataset/dataset.component";
import {CatalogComponent} from "./catalog.component";
import {catalogStates} from "./catalog.states";
import {ConnectorTypesComponent} from './connector-types/connector-types.component';

@NgModule({
    declarations: [
        ConnectorsComponent,
        ConnectorTypesComponent,
        DatasetComponent,
        CatalogComponent,
    ],
    imports: [
        CatalogApiModule,
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
        UIRouterModule.forChild({states: catalogStates})
    ]
})
export class CatalogModule {
}
