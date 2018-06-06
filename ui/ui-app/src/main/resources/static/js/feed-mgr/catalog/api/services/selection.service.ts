import {Injectable} from "@angular/core";

@Injectable()
export class SelectionService {

    private selections: Map<string, any> = new Map<string, any>();

    private lastPath: Map<string, any> = new Map<string, any>();

    /**
     * Stores selection for data source
     * @param {string} datasourceId
     * @param {any} selection
     */
    set(datasourceId: string, selection: any): void {
        this.selections.set(datasourceId, selection);
    }

    /**
     * Resets selection for data source
     * @param {string} datasourceId
     */
    reset(datasourceId: string): void {
        this.selections.delete(datasourceId);
    }

    /**
     * @param {string} datasourceId
     * @returns {any} selection for data source
     */
    get(datasourceId: string): any {
        return this.selections.get(datasourceId);
    }

    setLastPath(datasourceId: string, params: any):void {
        this.lastPath.set(datasourceId, params)
    }

    getLastPath(datasourceId: string): any {
        return this.lastPath.get(datasourceId)
    }
}
