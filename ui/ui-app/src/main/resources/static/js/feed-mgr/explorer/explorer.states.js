define(["require", "exports", "@uirouter/angular", "rxjs/operators/catchError", "./catalog/services/catalog.service", "./dataset/dataset.component", "./explorer.component"], function (require, exports, angular_1, catchError_1, catalog_service_1, dataset_component_1, explorer_component_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.explorerStates = [
        {
            name: "explorer",
            url: "/explorer",
            views: {
                "content": {
                    component: explorer_component_1.ExplorerComponent
                }
            },
            resolve: [
                { token: "connectors", deps: [catalog_service_1.CatalogService], resolveFn: function (catalog) { return catalog.getConnectors().toPromise(); } }
            ],
            data: {
                breadcrumbRoot: true,
                displayName: "Explorer"
            }
        },
        {
            name: "explorer.dataset",
            url: "/:dataSetId",
            component: dataset_component_1.DatasetComponent,
            resolve: [
                {
                    token: "dataSet",
                    deps: [catalog_service_1.CatalogService, angular_1.StateService],
                    resolveFn: function (catalog, state) {
                        return catalog.getDataSet(state.transition.params().dataSetId)
                            .pipe(catchError_1.catchError(function () { return state.go("explorer"); }))
                            .toPromise();
                    }
                }
            ]
        },
        {
            name: "explorer.dataset.preview.**",
            url: "/preview",
            loadChildren: "feed-mgr/explorer/dataset/preview-schema/preview-schema.module#PreviewSchemaModule"
        },
        {
            name: "explorer.dataset.upload.**",
            url: "/upload",
            loadChildren: "feed-mgr/explorer/dataset/upload/upload.module#UploadModule"
        }
    ];
});
//# sourceMappingURL=explorer.states.js.map