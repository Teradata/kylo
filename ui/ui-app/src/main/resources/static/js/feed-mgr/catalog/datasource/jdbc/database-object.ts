import {BrowserObject} from '../api/browser-object';
import {BrowserColumn} from '../api/browser-column';

enum DatabaseObjectType {
    Catalog = "catalog",
    Schema = "schema",
    Table = "table",
    Column = "column"
}

export class DatabaseObject extends BrowserObject {
    type: DatabaseObjectType;

    constructor(name: string, type: DatabaseObjectType) {
        super();
        this.name = name;
        this.type = type;
    }

    canBeParent(): boolean {
        return this.type !== DatabaseObjectType.Column;
    }
}

export class DatabaseObjectDescriptor {

    static COLUMNS: BrowserColumn[] = [
        {name: "name", label: "Name", sortable: true, filter: true},
    ];
}