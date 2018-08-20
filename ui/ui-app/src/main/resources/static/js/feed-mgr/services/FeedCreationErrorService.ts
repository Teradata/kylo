/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import * as _ from "underscore";
import { Injectable, Inject, Component } from '@angular/core';
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { TranslateService } from "@ngx-translate/core";

class FeedErrorModel {
        isValid: boolean = false;
        hasErrors: boolean = false;
        feedName: string = '';
        nifiFeed: any = {};
        message: string = '';
        feedErrorsData: any = {};
        feedErrorsCount: Number = 0;
        response: any = {};
}

@Injectable()
export class FeedCreationErrorService {
    constructor (private dialog: MatDialog,
                private translate: TranslateService) {}
  

        parseNifiFeedForErrors (nifiFeed:any, errorMap:any) {
            var count = 0;

            if (nifiFeed != null) {

                if (nifiFeed.errorMessages != null && nifiFeed.errorMessages.length > 0) {
                    nifiFeed.errorMessages.array.forEach((msg : any) => {
                        errorMap['FATAL'].push({category: 'General', message: msg});
                        count++;
                    });
                }

                if (nifiFeed.feedProcessGroup != null) {
                    nifiFeed.feedProcessGroup.errors.forEach((processor:any) => {
                        if (processor.validationErrors) {
                            processor.validationErrors.forEach((error:any) => {
                                let copy:any = {...error,...processor};
                                copy.validationErrors = null;
                                errorMap[error.severity].push(copy);
                                count++;
                            });
                        }
                    });
                }
                if (errorMap['FATAL'].length == 0) {
                    delete errorMap['FATAL'];
                }
                if (errorMap['WARN'].length == 0) {
                    delete errorMap['WARN'];
                }
            }
            return count;

        }

        buildErrorMapAndSummaryMessage () {
            var count = 0;
            var errorMap:any = {"FATAL": [], "WARN": []};
            if (this.feedError.nifiFeed != null && this.feedError.response.status < 500) {

                count = this.parseNifiFeedForErrors(this.feedError.nifiFeed, errorMap);
                this.feedError.feedErrorsData = errorMap;
                this.feedError.feedErrorsCount = count;

                if (this.feedError.feedErrorsCount == 1) {
                    //@TODO Ahmad Hassan use angular 5 internationalization
                    var errorMessage:string = "";
                    (() => { return this.translate.get('views.FeedCreationErrorService.iiwf').toPromise().then(value => {return errorMessage = value})})();
                    this.feedError.message = this.feedError.feedErrorsCount + errorMessage;
                    this.feedError.isValid = false;
                }
                else if (this.feedError.feedErrorsCount >=2  || this.feedError.feedErrorsCount <= 4) {

                    this.feedError.message = this.translate.instant('views.FeedCreationErrorService.Found') + this.feedError.feedErrorsCount + this.translate.instant('views.FeedCreationErrorService.iiwf2');
                    this.feedError.isValid = false;
                }
                else if (this.feedError.feedErrorsCount >= 5) {

                    this.feedError.message = this.translate.instant('views.FeedCreationErrorService.Found') + this.feedError.feedErrorsCount + this.translate.instant('views.FeedCreationErrorService.iiwf2');
                    this.feedError.isValid = false;
                }
                else {
                    this.feedError.isValid = true;
                }
            }
            else if (this.feedError.response.status === 502) {
                this.feedError.message = 'Error creating feed, bad gateway'
            } else if (this.feedError.response.status === 503) {
                this.feedError.message = 'Error creating feed, service unavailable'
            } else if (this.feedError.response.status === 504) {
                this.feedError.message = 'Error creating feed, gateway timeout'
            } else if (this.feedError.response.status === 504) {
                this.feedError.message = 'Error creating feed, HTTP version not supported'
            } else {
                this.feedError.message = 'Error creating feed.'
            }

        }


        feedError: FeedErrorModel = new FeedErrorModel();
        buildErrorData (feedName:any, response:any) {
            this.feedError.feedName = feedName;
            this.feedError.nifiFeed = response;
            this.feedError.response = response;
            this.buildErrorMapAndSummaryMessage();
            this.feedError.hasErrors = this.feedError.feedErrorsCount > 0;
        }
        parseNifiFeedErrors (nifiFeed:any, errorMap:any) {
            return this.parseNifiFeedForErrors(nifiFeed, errorMap);
        }
        reset () {
            this.feedError = new FeedErrorModel();
        }
        hasErrors () {
            return this.feedError.hasErrors;
        }
        showErrorDialog= () =>{

            let dialogRef = this.dialog.open(FeedErrorDialogController, {
                data: {feedError: this.feedError},
                panelClass: "full-screen-dialog"
              });
        }

}

/**
 * The Controller used for the Feed Saving Dialog
 */
@Component({
    selector: 'feed-error-dialog-controller',
    templateUrl: 'js/feed-mgr/feeds/define-feed/feed-error-dialog.html'
})
export class FeedErrorDialogController {

    feedName: string;
    message: string;
    createdFeed: string;
    isValid: boolean;
    feedErrorsData: string;
    feedErrorsCount: Number;

    ngOnInit() {

        var errorData = this.feedCreationErrorService.feedError;
        this.feedName = errorData.feedName;
        this.createdFeed = errorData.nifiFeed;
        this.isValid = errorData.isValid;
        this.message = errorData.message;
        this.feedErrorsData = errorData.feedErrorsData;
        this.feedErrorsCount = errorData.feedErrorsCount;
    }
    constructor(private dialogRef: MatDialogRef<FeedErrorDialogController>,
                private feedCreationErrorService: FeedCreationErrorService) {}

    hide () {
        this.dialogRef.close();
    };

    cancel () {
        this.dialogRef.close();
    };

    fixErrors () {
        this.dialogRef.close('fixErrors');
    }
}
