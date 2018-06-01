import {DatabaseObject, DatabaseObjectDescriptor} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';
import {BrowserColumn} from '../api/browser-column';

export class JdbcComponent extends BrowserComponent {

    getColumns(): BrowserColumn[] {
        return DatabaseObjectDescriptor.COLUMNS;
    }

    getSortByColumnName(): string {
        return this.columns[0].name;
    }


    getStateName(): string {
        return "catalog.datasource.jdbc";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/jdbc?path=";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new DatabaseObject(obj.name, obj.type);
    }
}
