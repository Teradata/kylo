import {BrowserObject} from '../api/browser-object';
import {BrowserColumn} from '../api/browser-column';

export enum DatabaseObjectType {
    Catalog = "CATALOG",
    Schema = "SCHEMA",
    Table = "TABLE",
    Column = "COLUMN"
}

export class DatabaseObject extends BrowserObject {
    type: DatabaseObjectType;

    constructor(name: string, type: DatabaseObjectType) {
        super();
        this.name = name;
        this.type = type;
    }

    canBeParent(): boolean {
        return this.type === DatabaseObjectType.Schema || this.type === DatabaseObjectType.Catalog;
    }
}

export class DatabaseObjectDescriptor {

    static COLUMNS: BrowserColumn[] = [
        {name: "name", label: "Name", sortable: true, filter: true},
    ];
}