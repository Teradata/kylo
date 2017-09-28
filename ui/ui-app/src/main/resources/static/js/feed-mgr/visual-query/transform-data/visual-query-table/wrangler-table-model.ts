import "fattable";
import {WranglerDataService} from "../services/wrangler-data.service";

export class WranglerTableModel extends fattable.SyncTableModel {

    constructor(private data: WranglerDataService) {
        super()
    }

    /**
     * Gets the value for the specified cell.
     *
     * @param {number} i the row number
     * @param {number} j the column number
     * @returns {VisualQueryTableCell|null} the cell object
     */
    getCellSync(i: number, j: number): string {
        return this.data.getCellSync(i, j);
    }

    /**
     * Gets the header of the specified column.
     *
     * @param {number} j the column number
     * @returns {VisualQueryTableHeader|null} the column header
     */
    getHeaderSync(j: number): string {
        return this.data.getHeaderSync(j);
    }
}
