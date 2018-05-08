var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/common/http", "@angular/core", "rxjs/observable/ArrayObservable", "rxjs/observable/ErrorObservable", "./data"], function (require, exports, http_1, core_1, ArrayObservable_1, ErrorObservable_1, data_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    // TODO testing only
    function uuidv4() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
    var CatalogService = /** @class */ (function () {
        function CatalogService(http) {
            this.http = http;
            this.dataSets = {};
        }
        CatalogService.prototype.createDataSet = function (connector) {
            var dataSet = {
                id: uuidv4(),
                connector: connector,
                connectorId: connector.id ? connector.id : connector.title
            };
            this.dataSets[dataSet.id] = dataSet;
            return ArrayObservable_1.ArrayObservable.of(dataSet);
        };
        /**
         * Gets the list of connectors.
         */
        CatalogService.prototype.getConnectors = function () {
            return ArrayObservable_1.ArrayObservable.of(data_1.connectors);
        };
        CatalogService.prototype.getDataSet = function (dataSetId) {
            if (this.dataSets[dataSetId]) {
                return ArrayObservable_1.ArrayObservable.of(this.dataSets[dataSetId]);
            }
            else {
                return ErrorObservable_1.ErrorObservable.create("Not found");
            }
        };
        CatalogService = __decorate([
            core_1.Injectable(),
            __metadata("design:paramtypes", [http_1.HttpClient])
        ], CatalogService);
        return CatalogService;
    }());
    exports.CatalogService = CatalogService;
});
//# sourceMappingURL=catalog.service.js.map