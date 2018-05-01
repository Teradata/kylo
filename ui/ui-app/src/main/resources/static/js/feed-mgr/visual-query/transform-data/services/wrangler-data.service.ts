import * as angular from "angular";

import {TransformValidationResult} from "../../wrangler/model/transform-validation-result";


const moduleName: string = require("feed-mgr/visual-query/module-name");

const PAGE_ROWS = 64;
const PAGE_COLS = 1000;

export class WranglerDataService {

    /**
     * The columns in this table with filters applied.
     */
    columns_: object[];

    /**
     * The data rows in this table with filters and sorting applied.
     */
    rows_: any[][];

    /**
     * The sort direction.
     */
    sortDirection_: ("asc" | "desc" | null) = null;

    /**
     * The index of the column being sorted.
     */
    sortIndex_: (number | null) = null;

    /**
     * Validation results for the current data.
     */
    validationResults: TransformValidationResult[][];

    asyncQuery: any;

    loading: boolean = false;

    deferred: any;

    promise: any;

    state: number;

    fetchTimeoutPromise: any = null;


    constructor(private $rootscope: any, private $q: angular.IQService, private $timeout: angular.ITimeoutService) {

    }

    fetchTimeout = <T>(callback: (...args: any[]) => T, interval: number) => {
        if (this.fetchTimeoutPromise != null) {
            this.$timeout.cancel(this.fetchTimeoutPromise);
        }
        this.fetchTimeoutPromise = this.$timeout(callback, interval);
    };

    cellPageName(i: number, j: number): string {
        var I = (i / PAGE_ROWS) | 0;
        var J = (j / PAGE_COLS) | 0;
        return JSON.stringify({"state": this.state, "coords": [I, J]});
    }

    headerPageName(j: number): string {
        var J = (j / PAGE_COLS) | 0;
        return JSON.stringify({"state": this.state, "j": J});
    };

    fetchCellPage(pageName: string, cb: any): void {

        this.fetchTimeout(() => {
            var coordsObj = JSON.parse(pageName);
            var I = coordsObj.coords[0];
            var J = coordsObj.coords[1];
            var self = this;

            this.asyncQuery(true, {
                firstRow: I * PAGE_ROWS,
                numRows: PAGE_ROWS,
                firstCol: J * PAGE_COLS,
                numCols: PAGE_COLS * 2
            }).then(() => {
                cb(function (i: number, j: number) {
                    return self.getCell(i - I * PAGE_ROWS, j - J * PAGE_COLS)
                });
            });
        }, 100);
    }

    /**
     * Gets the value for the specified cell.
     *
     * @param {number} i the row number
     * @param {number} j the column number
     * @returns {VisualQueryTableCell|null} the cell object
     */
    getCell(i: number, j: number): any {
        const column: any = this.columns_[j];
        if (column != undefined && i >= 0 && i < this.rows_.length) {
            const originalIndex = (this.rows_[i].length > this.columns_.length) ? this.rows_[i][this.columns_.length] : null;
            const validation = (this.validationResults != null && originalIndex < this.validationResults.length && this.validationResults[originalIndex] != null)
                ? this.validationResults[originalIndex].filter(result => result.field === column.headerTooltip)
                : null;
            return {
                column: j,
                field: column.name,
                row: i,
                validation: (validation !== null && validation.length > 0) ? validation : null,
                value: this.rows_[i][j]
            };
        } else {
            return null;
        }
    }

    /**
     * Gets the header of the specified column.
     *
     * @param {number} j the column number
     * @returns {VisualQueryTableHeader|null} the column header
     */
    getHeader(j: number): object {

        if (j >= 0 && j < this.columns_.length) {
            return angular.extend(this.columns_[j], {
                field: (this.columns_[j] as any).name,
                index: j,
                sort: {
                    direction: (this.sortIndex_ === j) ? this.sortDirection_ : null
                }
            });
        }
        return null;
    }


}

angular.module(moduleName).service("WranglerDataService", ["$rootScope", "$q", "$timeout", WranglerDataService]);


