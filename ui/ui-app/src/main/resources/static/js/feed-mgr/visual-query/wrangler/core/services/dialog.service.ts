import {Injectable} from "@angular/core";
import {TdDialogService} from "@covalent/core";
import "rxjs/add/operator/filter";
import {Observable} from "rxjs/Observable";

import {DateFormatConfig, DateFormatResponse, DialogService} from "../../api/services/dialog.service";
import {DateFormatDialog} from "../columns/date-format.component";

/**
 * Opens modal dialogs for alerting the user or receiving user input.
 */
@Injectable()
export class WranglerDialogService implements DialogService {

    constructor(private dialog: TdDialogService) {
    }

    /**
     * Opens a modal dialog for the user to input a date format string.
     *
     * @param config - dialog configuration
     * @returns the date format string
     */
    openDateFormat(config: DateFormatConfig): Observable<DateFormatResponse> {
        return this.dialog.open(DateFormatDialog, {data: config, panelClass: "full-screen-dialog"})
            .afterClosed()
            .filter(value => typeof value !== "undefined");
    }
}
