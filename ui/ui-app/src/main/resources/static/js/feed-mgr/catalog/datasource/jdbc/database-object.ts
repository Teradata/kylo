import {ITdDataTableColumn} from '@covalent/core/data-table';

enum DatabaseObjectType {
    Schema = "schema",
    Table = "table",
    Column = "column"
}

export class DatabaseObject {
    path: string;
    name: string;
    type: DatabaseObjectType;

    constructor(name: string, type: DatabaseObjectType) {
        this.name = name;
        this.type = type;
    }

    isColumn(): boolean {
        return this.type === DatabaseObjectType.Column;
    }
}

export class DatabaseObjectDescriptor {

    static COLUMNS: ITdDataTableColumn[] = [
        {name: "name", label: "Name", sortable: true, filter: true},
    ];
}