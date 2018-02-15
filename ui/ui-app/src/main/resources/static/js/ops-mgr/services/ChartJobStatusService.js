define(["require", "exports", "angular", "../module-name", "./IconStatusService", "./Nvd3ChartService"], function (require, exports, angular, module_name_1, IconStatusService_1, Nvd3ChartService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ChartJobStatusService = /** @class */ (function () {
        function ChartJobStatusService(IconService, Nvd3ChartService) {
            this.IconService = IconService;
            this.Nvd3ChartService = Nvd3ChartService;
            this.renderEndUpdated = {};
            this.toChartData = function (jobStatusCountResponse) {
                return this.Nvd3ChartService.toLineChartData(jobStatusCountResponse, [{ label: 'status', value: 'count' }], 'date', this.IconService.colorForJobStatus);
            };
            this.shouldManualUpdate = function (chart) {
                if (this.renderEndUpdated[chart] == undefined) {
                    this.renderEndUpdated[chart] = chart;
                    return true;
                }
                else {
                    return false;
                }
            };
        }
        ;
        ;
        return ChartJobStatusService;
    }());
    exports.default = ChartJobStatusService;
    angular.module(module_name_1.moduleName)
        .service('IconService', [IconStatusService_1.default])
        .service('Nvd3ChartService', ["$timeout", "$filter", Nvd3ChartService_1.default])
        .service('ChartJobStatusService', ["IconService", "Nvd3ChartService", ChartJobStatusService]);
});
//# sourceMappingURL=ChartJobStatusService.js.map