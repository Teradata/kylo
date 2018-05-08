var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
define(["require", "exports", "@angular/common/http", "@angular/core", "./pipes/file-size.pipe", "./services/catalog.service", "./services/file-manager.service"], function (require, exports, http_1, core_1, file_size_pipe_1, catalog_service_1, file_manager_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var CatalogModule = /** @class */ (function () {
        function CatalogModule() {
        }
        CatalogModule = __decorate([
            core_1.NgModule({
                declarations: [
                    file_size_pipe_1.FileSizePipe
                ],
                exports: [
                    file_size_pipe_1.FileSizePipe
                ],
                imports: [
                    http_1.HttpClientModule
                ],
                providers: [
                    catalog_service_1.CatalogService,
                    file_manager_service_1.FileManagerService
                ]
            })
        ], CatalogModule);
        return CatalogModule;
    }());
    exports.CatalogModule = CatalogModule;
});
//# sourceMappingURL=catalog.module.js.map