import {DatabaseObject, DatabaseObjectDescriptor} from './database-object';
import {BrowserComponent} from '../api/browser.component';
import {BrowserObject} from '../api/browser-object';
import {BrowserColumn} from '../api/browser-column';
import {Node} from '../api/node';

export class JdbcComponent extends BrowserComponent {

    init(): void {
        this.initData();
    }

    getColumns(): BrowserColumn[] {
        return DatabaseObjectDescriptor.COLUMNS;
    }

    getSortByColumnName(): string {
        return this.columns[0].name;
    }

    getStateName(): string {
        return "catalog.datasource.connection";
    }

    getUrl(): string {
        return "/proxy/v1/catalog/datasource/" + this.datasource.id + "/tables";
    }

    mapServerResponseToBrowserObject(obj: any): BrowserObject {
        return new DatabaseObject(obj.name, obj.type);
    }

    createRootNode(): Node {
        return new Node(this.datasource.template.options.url);
    }
}
