import {Injectable} from "@angular/core";

@Injectable()
export class SelectionService {

    private selections: Map<string, Map<any, any>>;

    constructor() {
        console.log('new SelectionService');
        this.selections = new Map<string, Map<any, any>>();
    }

    /**
     * Sets selected items for data source at given location
     * @param {string} datasourceId
     * @param {string} location
     * @param {any[]} selectedItems
     */
    set(datasourceId: string, location: any, selectedItems: any): void {
        console.log('set for data source ' + datasourceId);
        let selection = this.selections.get(datasourceId);
        if (selection === undefined) {
            selection = new Map<any, any>();
            this.selections.set(datasourceId, selection);
        }
        selection.set(location, selectedItems);
    }

    /**
     * Resets selection for data source
     * @param {string} datasourceId
     */
    reset(datasourceId: string): void {
        console.log('reset for data source ' + datasourceId);
        console.log('selections before', this.selections);
        this.selections.delete(datasourceId);
        console.log('selections after', this.selections);
    }

    /**
     * @param {string} datasourceId
     * @param {string} location
     * @returns {any[]} number of items selected for data source on given location or empty array if
     * there is nothing selected. This may be different from total number of selected rows.
     */
    get(datasourceId: string, location: any): any {
        let selection = this.selections.get(datasourceId);
        let result;
        if (selection) {
            result = selection.get(location);
        }
        return result ? result : new Map<any, any>();
    }

    /**
     * @param {string} datasourceId
     * @returns {any[]} all selected items for data source in all locations
     */
    getAll(datasourceId: string): Map<any, any> {
        let selection = this.selections.get(datasourceId);
        return selection ? selection : new Map<any, any>();
    }
}
