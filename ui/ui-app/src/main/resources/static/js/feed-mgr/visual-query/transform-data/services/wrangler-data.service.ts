import * as angular from "angular";

const moduleName: string = require("feed-mgr/visual-query/module-name");

export class WranglerDataService {

    /**
     * The columns in this table with filters applied.
     */
    columns_: object[];

    /**
     * The data rows in this table with filters and sorting applied.
     */
    rows_: object[];

    /**
     * The sort direction.
     */
    sortDirection_: ("asc" | "desc" | null) = null;

    /**
     * The index of the column being sorted.
     */
    sortIndex_: (number | null) = null;

    /**
     * Gets the value for the specified cell.
     *
     * @param {number} i the row number
     * @param {number} j the column number
     * @returns {VisualQueryTableCell|null} the cell object
     */
    getCellSync(i: number, j: number): any {
        const column: any = this.columns_[j];
        if (i >= 0 && i < this.rows_.length) {
            return {
                column: j,
                row: i,
                value: this.rows_[i][column.name]
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
    getHeaderSync(j: number): any {
        if (j >= 0 && j < this.columns_.length) {
            return angular.extend(this.columns_[j], {
                field: (this.columns_[j] as any).name,
                index: j,
                sort: {
                    direction: (this.sortIndex_ === j) ? this.sortDirection_ : null
                }
            });
        } else {
            return null;
        }
    }
}

angular.module(moduleName).service("WranglerDataService", WranglerDataService);
