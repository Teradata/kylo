import {BrowserObject} from '../../api/models/browser-object';
import {BrowserColumn} from '../../api/models/browser-column';


export enum DatabaseObjectType {
    Catalog = "CATALOG",
    Schema = "SCHEMA",
    Table = "TABLE",
    View = "VIEW",
    Column = "COLUMN",
    ManagedTable = "MANAGED_TABLE",
    ExternalTable = "EXTERNAL_TABLE"
}

export namespace DatabaseObjectType {
    export function isTableType(type:DatabaseObjectType){
        return type == DatabaseObjectType.Table || type == DatabaseObjectType.ManagedTable || type == DatabaseObjectType.ExternalTable || type == DatabaseObjectType.View
    }

    export function isSchema(type:DatabaseObjectType){
        return type == DatabaseObjectType.Schema || type == DatabaseObjectType.Catalog;
    }
    export function parse(type: string): DatabaseObjectType {
        return DatabaseObjectType[type];
    }
}

export class DatabaseObject extends BrowserObject {
    type: DatabaseObjectType;
    catalog: string;
    schema: string;
    qualifiedIdentifier: string;

    constructor(name: string, type: DatabaseObjectType, catalog: string, schema: string, qualifiedIdentifier: string) {
        super();
        this.name = name;
        this.type = type;
        this.catalog = catalog;
        this.schema = schema;
        this.qualifiedIdentifier = qualifiedIdentifier;
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

    getIcon() : string {
        if(DatabaseObjectType.isTableType(this.type)){
            return "table_grid";
        }
        else if(DatabaseObjectType.isSchema(this.type)){
            return "fa-database"
        }
        else {
            return "fa-columns"
        }
    }

}

export class DatabaseObjectDescriptor {

    static NAME_COLUMN ="name";
    static TYPE_COLUMN ="type";

    static COLUMNS: BrowserColumn[] = [
        {name: "selection", label: " ", sortable: false, width: 40, filter: false, icon: false},
        {name: DatabaseObjectDescriptor.TYPE_COLUMN, label: "Type", sortable: false, filter: false, width: 100, icon:true,tooltip:"true"},
        {name: DatabaseObjectDescriptor.NAME_COLUMN, label: "Name", sortable: true, filter: true},
    ];
}
