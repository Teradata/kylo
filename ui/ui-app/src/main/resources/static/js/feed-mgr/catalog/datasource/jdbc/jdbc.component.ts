import {DatabaseObject, DatabaseObjectDescriptor} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';
import {ITdDataTableColumn} from '@covalent/core/data-table';

export class JdbcComponent extends BrowserComponent {

    getColumns(): ITdDataTableColumn[] {
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
