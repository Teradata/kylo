import {Component, OnInit, ViewChild} from "@angular/core";
import {TemplateMetadata, TemplateRepository} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {StateService} from "@uirouter/angular";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {TemplatePublishDialog} from "../dialog/template-publish-dialog";
import {MatDialog} from "@angular/material/dialog";
import {TemplateUpdatesDialog} from "../dialog/template-updates-dialog";

/**
 * List templates from repository ready for installation.
 */
@Component({
    selector: "list-templates",
    templateUrl: "./list.component.html"
})
export class ListTemplatesComponent implements OnInit {

    constructor(private templateService: TemplateService,
                private dataTableService: TdDataTableService,
                private state: StateService,
                private dialog: MatDialog) {
    }

    loading:boolean = true;
    selectedTemplate: TemplateMetadata;
    errorMsg: string = "";
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    /**
     * List of available templates
     */
    templates: TemplateMetadata[] = [];
    repositories: TemplateRepository[] = [];
    selectedRepository: TemplateRepository;

    dataSource = new MatTableDataSource();

    public ngOnInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.init();
    }

    private init() {
        this.templateService.getRepositories().subscribe(
            (repos: TemplateRepository[]) => {
                this.repositories = repos;
                if(this.repositories.length > 0){
                    this.selectedRepository = this.repositories[0];
                    this.loadTemplates();
                }
            }
        );
    }

    loadTemplates() {

        this.templateService.getTemplatesInRepository(this.selectedRepository).subscribe(
            (data: TemplateMetadata[]) => {
                this.templates = data;
                this.dataSource.data = data;
                this.loading = false;
            },
            (error: any) => {
                console.log(error);
                if(error.developerMessage)
                    this.errorMsg += error.developerMessage;

                console.log(this.errorMsg);
                this.loading = false;
            }
        );
    }

    /*
     * download template from repository.
     */
    downloadTemplate(template: TemplateMetadata) {
        this.templateService.downloadTemplate(template).subscribe(blob => {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = template.fileName;
            link.click();
        });
    }

    updateTemplate(template: TemplateMetadata) {
        this.importTemplate(template);
    }

    importTemplate(template: TemplateMetadata) {
        this.selectedTemplate = template;
        let param = {"template": template};
        this.state.go("import-template", param);
    }

    pageSize: number = 50;
    currentPage: number = 1;
    searchTerm: string = '';

    search(filter: string): void {
        this.dataSource.filter = filter.trim().toLowerCase();
    }

    viewUpdates(template: TemplateMetadata): void {

        this.dialog.open(TemplateUpdatesDialog, {
            data: {updates: template.updates},
            width: '40%'
        });
    }
}
