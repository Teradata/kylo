import {SlaEmailTemplate} from "./sla-email-template.model";
import {TdLoadingService} from "@covalent/core/loading";
import {TdDataTableService} from "@covalent/core/data-table";
import {TranslateService} from "@ngx-translate/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {Component,Inject, OnInit} from "@angular/core";
import {AccessControlService} from "../../../services/AccessControlService";
import {MatSnackBar} from "@angular/material/snack-bar";
import {StateService} from "@uirouter/angular";
import {SlaEmailTemplateService} from "./sla-email-template.service";
import {DataSourcesComponent} from "../../catalog/datasources/datasources.component";

@Component({
    selector: 'sla-email-templates',
    styleUrls: ['./sla-email-templates.component.scss'],
    templateUrl: './sla-email-templates.component.html'
})
export class SlaEmailTemplatesComponent implements OnInit{

    static readonly LOADER = "SlaEmailTemplatesComponent.LOADER";

    templates:SlaEmailTemplate[] = [];

    filterString:string;

    filteredTemplates:SlaEmailTemplate[] = []

    allowEdit:boolean = false;

    loading:boolean;



    constructor( private slaEmailTemplatesService:SlaEmailTemplateService,
                 private dataTable: TdDataTableService,
                 private dialog: TdDialogService,
                 private loadingService: TdLoadingService,
                 private state: StateService,
                 @Inject("AccessControlService") private accessControlService: AccessControlService,
                 private snackBarService: MatSnackBar,
                 private translateService: TranslateService) {

        accessControlService.getUserAllowedActions()
            .then((actionSet: any) => this.allowEdit = accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions));
    }

    public ngOnInit() {
        this.loading = true;
        this.loadingService.register(SlaEmailTemplatesComponent.LOADER)
        this.slaEmailTemplatesService.getExistingTemplates().subscribe((templates:SlaEmailTemplate[]) => {
            this.templates = templates;
            this.filterTemplates();
            this.loading = false;
            this.loadingService.resolve(SlaEmailTemplatesComponent.LOADER)
        }, (error1:any) => {
            this.snackBarService.open("Error loading email templates",null,{duration:5000});
            this.loading = false
            this.loadingService.resolve(SlaEmailTemplatesComponent.LOADER)
        })

    }

    search(txt:string){
        this.filterString = txt;
        this.filterTemplates();

    }
    selectTemplate(template:SlaEmailTemplate){
        this.state.go("^.edit",{templateId:template.id, template:template});
    }


    /**
     * filter the templates
     */
    private filterTemplates() {
        let filteredTemplates = this.dataTable.filterData(this.templates, this.filterString, true);
        filteredTemplates = this.dataTable.sortData(filteredTemplates, "name");
        this.filteredTemplates = filteredTemplates;
    }

}