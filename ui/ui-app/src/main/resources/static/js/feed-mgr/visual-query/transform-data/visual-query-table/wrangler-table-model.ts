import "fattable";
import {WranglerDataService} from "../services/wrangler-data.service";

export class WranglerTableModel extends fattable.PagedAsyncTableModel {

    constructor(private data: WranglerDataService) {
        super();
    }

    getHeader(j: number, cb: any): void {
        cb(this.data.getHeader(j));
    };

    cellPageName(i: number, j: number): string {
        return this.data.cellPageName(i, j);
    }

    fetchCellPage(pageName: string, cb: any): void {
        this.data.fetchCellPage(pageName, cb);
    }

    headerPageName(j: number): string {
        return this.data.headerPageName(j);
    };


}