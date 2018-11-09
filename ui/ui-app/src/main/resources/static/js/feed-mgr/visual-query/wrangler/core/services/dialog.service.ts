import {Injectable} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import "rxjs/add/operator/filter";
import {Observable} from "rxjs/Observable";

import {DateFormatConfig, DateFormatResponse, DialogService} from "../../api/services/dialog.service";
import {DateFormatDialog} from "../columns/date-format.component";
import {DynamicFormDialogData} from "../../../../../../lib/dynamic-form/simple-dynamic-form/dynamic-form-dialog-data";
import {SimpleDynamicFormDialogComponent} from "../../../../../../lib/dynamic-form/simple-dynamic-form/simple-dynamic-form-dialog.component";
import {ColumnForm} from "../columns/column-form";

/**
 * Opens modal dialogs for alerting the user or receiving user input.
 */
@Injectable()
export class WranglerDialogService implements DialogService {

    topOffset = '0px';
    width ='350px'

    constructor(private dialog: TdDialogService) {
    }

    /**
     * Opens a modal dialog for the user to input a date format string.
     *
     * @param config - dialog configuration
     * @returns the date format string
     */
    openDateFormat(config: DateFormatConfig): Observable<DateFormatResponse> {
        return this.dialog.open(DateFormatDialog, {data: config, panelClass: "full-screen-dialog",height:'100%',width:this.width,position:{top:this.topOffset,right:'0'}})
            .afterClosed()
            .filter(value => typeof value !== "undefined");
    }

    openColumnForm(data:ColumnForm):Observable<any>{
      let dialogData:DynamicFormDialogData = new DynamicFormDialogData(data.formConfig)
      return  this.dialog.open(SimpleDynamicFormDialogComponent,{data:dialogData, panelClass: "full-screen-dialog",height:'100%',width:this.width,position:{top:this.topOffset,right:'0'}})
            .afterClosed()
            .filter(value => typeof value !== "undefined");
    }


}
