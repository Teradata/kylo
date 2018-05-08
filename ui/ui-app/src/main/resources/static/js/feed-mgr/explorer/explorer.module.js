var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/common", "@angular/core", "@angular/flex-layout", "@angular/material/card", "@angular/material/divider", "@angular/material/list", "@angular/material/tabs", "@angular/material/toolbar", "@covalent/core/data-table", "@covalent/core/dialogs", "@covalent/core/layout", "@covalent/core/loading", "@covalent/core/search", "@uirouter/angular", "../../common/common.module", "./catalog/catalog.module", "./connectors/connectors.component", "./dataset/dataset.component", "./explorer.component", "./explorer.states"], function (require, exports, common_1, core_1, flex_layout_1, card_1, divider_1, list_1, tabs_1, toolbar_1, data_table_1, dialogs_1, layout_1, loading_1, search_1, angular_1, common_module_1, catalog_module_1, connectors_component_1, dataset_component_1, explorer_component_1, explorer_states_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ExplorerModule = /** @class */ (function () {
        function ExplorerModule() {
        }
        ExplorerModule = __decorate([
            core_1.NgModule({
                declarations: [
                    connectors_component_1.ConnectorsComponent,
                    dataset_component_1.DatasetComponent,
                    explorer_component_1.ExplorerComponent,
                ],
                imports: [
                    catalog_module_1.CatalogModule,
                    common_1.CommonModule,
                    data_table_1.CovalentDataTableModule,
                    dialogs_1.CovalentDialogsModule,
                    layout_1.CovalentLayoutModule,
                    loading_1.CovalentLoadingModule,
                    search_1.CovalentSearchModule,
                    flex_layout_1.FlexLayoutModule,
                    common_module_1.KyloCommonModule,
                    card_1.MatCardModule,
                    divider_1.MatDividerModule,
                    list_1.MatListModule,
                    tabs_1.MatTabsModule,
                    toolbar_1.MatToolbarModule,
                    angular_1.UIRouterModule.forChild({ states: explorer_states_1.explorerStates })
                ]
            })
        ], ExplorerModule);
        return ExplorerModule;
    }());
    exports.ExplorerModule = ExplorerModule;
});
//# sourceMappingURL=explorer.module.js.map