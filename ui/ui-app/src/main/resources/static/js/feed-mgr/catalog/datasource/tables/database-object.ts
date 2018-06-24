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
    catalog: string;
    schema: string;

    constructor(name: string, type: DatabaseObjectType, catalog: string, schema: string) {
        super();
        this.name = name;
        this.type = type;
        this.catalog = catalog;
        this.schema = schema;
    }

    canBeParent(): boolean {
        return this.type === DatabaseObjectType.Schema || this.type === DatabaseObjectType.Catalog;
    }

    getPath(): string {
        let path = [];
        if (this.catalog) {
            path.push("catalog=" + this.catalog);
        } else if (this.schema) {
            path.push("schema=" + this.schema);
        }
        path.push("type=" + this.type);
        path.push("name=" + this.name);
        return path.join("&");
    }
}

export class DatabaseObjectDescriptor {

    static COLUMNS: BrowserColumn[] = [
        {name: "type", label: "Type", sortable: false, filter: false, width: 100},
        {name: "name", label: "Name", sortable: true, filter: true},
    ];
}