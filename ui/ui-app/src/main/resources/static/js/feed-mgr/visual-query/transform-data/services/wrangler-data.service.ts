import * as angular from "angular";
import * as _ from "underscore";

import {TransformValidationResult} from "../../wrangler/model/transform-validation-result";
import {PageSpec} from "../../wrangler/query-engine";
import {ScriptState} from "../../wrangler";

const moduleName: string = require("feed-mgr/visual-query/module-name");

const PAGE_ROWS = 64;
const PAGE_COLS = 1000;

export class WranglerDataService {

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

    columns_ : any[];

    isLoading: boolean;


    /**
     * Table state (function index) for
     */
    state: number;

    constructor(private $rootscope: any, private $q: angular.IQService) {

    }

    cellPageName(i: number, j: number): string {
        var I = (i / PAGE_ROWS) | 0;
        var J = (j / PAGE_COLS) | 0;
        var name =  JSON.stringify({"state": this.state, "coords": [I, J]});
        return name;
    }

    headerPageName(j: number): string {
        var J = (j / PAGE_COLS) | 0;
        return JSON.stringify({"state": this.state, "j": J});
    };

    fetchCellPage(pageName: string, cb: any): void {

        var asyncFn = _.debounce(() => {
            var coordsObj = JSON.parse(pageName);
            var I = coordsObj.coords[0];
            var J = coordsObj.coords[1];
            var self = this;

            this.asyncQuery(new PageSpec( {
                firstRow: I * PAGE_ROWS,
                numRows: PAGE_ROWS,
                firstCol: J * PAGE_COLS,
                numCols: PAGE_COLS
            })).then((result: ScriptState<any>) => {
                this.state = result.tableState;
                var rows = result.rows;
                var validationResults = angular.copy(result.validationResults);

                cb((i: number, j: number) =>  {
                    return self.getCell(i - I * PAGE_ROWS, j - J * PAGE_COLS, rows, this.columns_, validationResults)
                });
            });
        },100);
        asyncFn();

    }

    /**
     * Gets the value for the specified cell.
     *
     * @param {number} i the row number
     * @param {number} j the column number
     * @returns {VisualQueryTableCell|null} the cell object
     */
    getCell(i: number, j: number, rows: object[][], cols: object[], validationResults : TransformValidationResult[][]): any {
        const column: any = cols;
        if (column != undefined && i >= 0 && i < rows.length) {
            /*
            TODO: Add back in
            const originalIndex = (rows[i].length > cols.length) ? rows[i][cols.length] : null;
            const validation = (validationResults != null && originalIndex < validationResults.length && validationResults[originalIndex] != null)
                ? validationResults[originalIndex].filter(result => result.field === column.headerTooltip)
                : null;
                */
            const validation : any = null;
            return {
                column: j,
                field: column.name,
                row: i,
                validation: (validation !== null && validation.length > 0) ? validation : null,
                value: rows[i][j]
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

        if (j >= 0 && this.columns_ && j < this.columns_.length) {
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

angular.module(moduleName).service("WranglerDataService", ["$rootScope", "$q",  WranglerDataService]);


