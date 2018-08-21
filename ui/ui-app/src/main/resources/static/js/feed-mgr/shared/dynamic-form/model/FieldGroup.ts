import {FieldConfig} from "./FieldConfig";


export enum Layout {
   COLUMN="column", ROW="row"
}
export class FieldGroup  {

    fields:FieldConfig<any>[] = []

    constructor(public layout:Layout = Layout.COLUMN) {
        this.fields = [];
    }


}