import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {QueryResultColumn} from "../../wrangler";

export class SchemaLayoutDialogData {

    constructor(public items: QueryResultColumn[]) {

    }
}

export class ColumnItem {

    public editMode: boolean = false;
    public origIndex: number;
    public newName: string;
    public newType: string;
    public deleted: boolean = false;

    constructor(public origName: string,
                public origType: string) {
        this.newName = this.origName;
        this.newType = this.origType;
    }

    isChanged(): boolean {
        return (this.newName != this.origName || this.isTypeChanged());
    }

    isTypeChanged(): boolean {
        return (this.newType != this.origType);
    }
}

@Component({
    templateUrl: 'js/feed-mgr/visual-query/transform-data/profile-stats/schema-layout-dialog.html',
    styleUrls: ["js/feed-mgr/visual-query/transform-data/profile-stats/column-analysis.css"]
})
export class SchemaLayoutDialog {

    public columns: ColumnItem[] = [];

    public trash: ColumnItem[] = [];

    public isChanged: boolean = false;


    // @ts-ignore
    constructor(private dialog: MatDialogRef<SchemaLayoutDialog>, @Inject(MAT_DIALOG_DATA) public data: SchemaLayoutDialogData) {
        for (let col of data.items) {
            this.columns.push(new ColumnItem( col.field, col.dataType));
        }
    }

    editMode(i: number): void {
        this.columns[i].editMode = true;
        this.isChanged = true;
    }

    remove(i: number): void {
        this.isChanged = true;
        this.columns[i].origIndex = i;
        this.columns[i].deleted = true;
        //this.trash.push(this.columns[i]);
        //this.columns.splice(i, 1)
    }

    restore(i: number): void {
        this.columns[i].deleted = false;
        //this.columns.splice(this.trash[i].origIndex, 1, this.trash[i]);
        //this.trash.splice(i, 1);
    }

    setType(i: number, type: string) {
        this.columns[i].newType = type;
    }

    castOptions() : Array<string> {
        let values : Array<string> = ['string', 'double'];
        return values;
    }

    apply() {
        this.columns = this.columns.filter( (v:ColumnItem) => { return !v.deleted});
        this.dialog.close(this.columns);
    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.dialog.close(null);
    }
}
