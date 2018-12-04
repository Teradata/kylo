import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {QueryResultColumn} from "../../wrangler";


export class SchemaLayoutDialogData {

    constructor(public items: QueryResultColumn[]) {

    }
}

export class ColumnItem {

    public editMode: boolean = false;
    public newName: string;
    public newType: string;
    public typeIcon: string;
    public deleted: boolean = false;

    constructor(public origName: string,
                public origType: string) {
        this.newName = this.origName;
        this.newType = this.origType;
        switch (this.origType) {
            case 'string':
                this.typeIcon = 'mdi-format-color-text';
                break;
            case 'float':
            case 'double':
            case 'long':
            case 'bigint':
            case 'tinyint':
            case 'smallint':
            case 'int':
                this.typeIcon = "mdi-pound";
                break;
            case 'timestamp':
                this.typeIcon = "mdi-clock-outline"
                break;
            case 'date':
                this.typeIcon = "mdi-calendar";
                break;
            case 'boolean':
                this.typeIcon = "mdi-toggle-switch-off-outline";
                break;
            case 'blob':
            case 'binary':
                this.typeIcon = "mdi-file-image";
            default:
                if (this.origType.indexOf("array") > -1) {
                    this.typeIcon = "mdi-code-brackets";
                } else if (this.origType.indexOf("struct") > -1) {
                    this.typeIcon = "mdi-code-braces";
                } else {
                    this.typeIcon = "mdi-google-circles-group";
                }
                break;
        }

    }
}

@Component({
    templateUrl: './schema-layout-dialog.html',
    styleUrls: ["./schema-layout-dialog.scss"]
})
export class SchemaLayoutDialog {

    public columns: ColumnItem[] = [];

    public isChanged: boolean = false;

    source: any;

    // @ts-ignore
    constructor(private dialog: MatDialogRef<SchemaLayoutDialog>, @Inject(MAT_DIALOG_DATA) public data: SchemaLayoutDialogData) {
        for (let col of data.items) {
            this.columns.push(new ColumnItem(col.field, col.dataType));
        }
    }

    editMode(i: number): void {
        this.columns[i].editMode = true;
        this.isChanged = true;
    }

    removeMovedItem(item: any, items: ColumnItem[]) {
        let index = items.indexOf(item);
        items.splice(index, 1);
        this.columns = items;
        this.isChanged = true;
    }


    remove(i: number): void {
        this.isChanged = true;
        this.columns[i].deleted = true;
    }

    restore(i: number): void {
        this.isChanged = true;
        this.columns[i].deleted = false;
    }

    setType(i: number, type: string) {
        this.isChanged = true;
        this.columns[i].newType = type;
    }

    setName(item: ColumnItem, newName: any) {
        item.newName = newName;
        this.isChanged = true;
    }

    castOptions(): Array<string> {
        let values: Array<string> = ['string', 'double'];
        return values;
    }

    apply() {
        this.columns = this.columns.filter((v: ColumnItem) => {
            return !v.deleted
        });
        this.dialog.close(this.columns);
    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.dialog.close(null);
    }
}
