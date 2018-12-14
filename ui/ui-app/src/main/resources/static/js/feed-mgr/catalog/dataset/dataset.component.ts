import {Component, Inject, Input, OnInit} from "@angular/core";
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {LoadingMode, LoadingType, TdLoadingService} from "@covalent/core/loading";
import {TranslateService} from "@ngx-translate/core";
import {StateService} from "@uirouter/angular";

import {AccessControlService} from "../../../services/AccessControlService";
import {CatalogService} from "../api/services/catalog.service";
import {Dataset} from '../api/models/dataset';


/**
 * Displays available dataset
 */
@Component({
    selector: "catalog-dataset",
    styleUrls: ["./dataset.component.scss"],
    templateUrl: "./dataset.component.html"
})
export class DatasetComponent implements OnInit {

    static readonly LOADER = "DataSourcesComponent.LOADER";
    private static topOfPageLoader: string = "DataSourcesComponent.topOfPageLoader";

    /**
     * List of available data sources
     */
    @Input("dataset")
    public dataset: Dataset;

    @Input("loading")
    public loading: boolean;

    @Input()
    public stateParams: {};

    editingDescription = false;

    constructor(private catalog: CatalogService,
                private dataTable: TdDataTableService,
                private dialog: TdDialogService,
                private loadingService: TdLoadingService,
                private state: StateService,
                @Inject("AccessControlService") private accessControlService: AccessControlService,
                private snackBarService: MatSnackBar,
                private translateService: TranslateService) {
        this.loadingService.create({
            name: DatasetComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });

        // accessControlService.getUserAllowedActions()
        //     .then((actionSet: any) => this.allowEdit = accessControlService.hasAction(AccessControlService.DATASET_EDIT, actionSet.actions));
    }


    public ngOnInit() {
        console.log("dataset", this.dataset);
    }

    // isEditable(datasource: DataSource): boolean {
    //     return (this.allowEdit && !this.readOnly
    //         && this.hasEditEntityAccess(datasource));
    // }
}
