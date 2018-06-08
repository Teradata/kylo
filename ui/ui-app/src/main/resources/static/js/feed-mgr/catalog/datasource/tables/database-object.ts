import {BrowserObject} from '../../api/models/browser-object';
import {BrowserColumn} from '../../api/models/browser-column';

export enum DatabaseObjectType {
    Catalog = "CATALOG",
    Schema = "SCHEMA",
    Table = "TABLE",
    View = "VIEW",
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
        {name: "type", label: "Type", sortable: false, filter: false, width: 100},
        {name: "name", label: "Name", sortable: true, filter: true},
    ];
}