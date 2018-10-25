import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {QueryResultColumn} from "../../wrangler";
import {ColumnUtil} from "../../wrangler/core/column-util";


export class QuickCleanDialogData {

    constructor(public items: QueryResultColumn[]) {

    }
}

export class OperationItem {

    public selected: boolean;

    public choice : string;

    public replacementValue: string;

    constructor(
                public type: string,
                public func: any,
                public label: string) {
    }
}

@Component({
    templateUrl: './quick-clean-dialog.html',
    styleUrls: ["./quick-clean-dialog.scss"]
})
export class QuickCleanDialog {

    public operations: OperationItem[] = [];

    // @ts-ignore
    constructor(private dialog: MatDialogRef<QuickCleanDialog>, @Inject(MAT_DIALOG_DATA) public data: QuickCleanDialogData) {

        this.operations.push(new OperationItem("string", function(s:string,o:OperationItem) { return `ltrim(${s})`}, "Trim leading whitespace"));
        this.operations.push(new OperationItem("string", function(s:string,o:OperationItem) { return `rtrim(${s})`}, "Trim trailing whitespace"));
        this.operations.push(new OperationItem("string", function(s:string,o:OperationItem) { return `${o.choice}(${s})`},"Normalize"));
        this.operations.push(new OperationItem("string", function(s:string,o:OperationItem) { return `when( isnull(${s}), '${o.replacementValue}').otherwise(${s})`}, "Replace" +
        " empty strings"));
        this.operations.push(new OperationItem("numeric", function(s:string,o:OperationItem) { return `when( isnull(${s}), ${o.replacementValue}).otherwise(${s})`}, "Replace empty" +
        " numeric"));
    }


    apply() {
        let cols = [];
        let script : string = null;
        let changed : boolean = false;
        for (let col of this.data.items) {
            let last = col.displayName;
            switch (col.dataType) {
                case 'string':
                    for (let op of this.operations) {
                        if ((op.selected || op.choice) && op.type=='string') {
                            changed = true;
                            last = op.func(last, op);
                        }
                    }
                    cols.push(`(${last}).as('${col.displayName}')`)
                    break;
                case 'float':
                case 'double':
                case 'long':
                case 'bigint':
                case 'tinyint':
                case 'smallint':
                case 'int':
                    for (let op of this.operations) {
                        if (op.selected && op.type=='numeric') {
                            changed = true;
                            last = op.func(last, op);
                        }
                    }
                    cols.push(`(${last}).as('${col.displayName}')`)
                    break;
                default:
                    cols.push(`(${last}).as('${col.displayName}')`)
                    break;
            }
        }
        if (changed) {
            script = `select(${cols.join(",")})`
        }
        this.dialog.close(script);
    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.dialog.close(null);
    }
}
