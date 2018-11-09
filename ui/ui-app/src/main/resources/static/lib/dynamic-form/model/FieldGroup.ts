import {FieldConfig} from "./FieldConfig";


export enum Layout {
   COLUMN="column", ROW="row"
}
export class FieldGroup  {

    private DEFAULT_LAYOUT = "start stretch";
    fields:FieldConfig<any>[] = [];
    layoutAlign: string;

    constructor(public layout:Layout = Layout.COLUMN) {
        this.fields = [];
    }

    setLayoutAlign(layoutAlign: string):void {
        this.layoutAlign = layoutAlign ? layoutAlign : this.DEFAULT_LAYOUT;
    }

}