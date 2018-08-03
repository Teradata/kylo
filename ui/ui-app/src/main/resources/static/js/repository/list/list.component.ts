import {Component, OnInit, ViewChild} from "@angular/core";
import {TemplateMetadata} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {StateService} from "@uirouter/angular";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";

/**
 * List templates from repository ready for installation.
 */
@Component({
    selector: "list-templates",
    templateUrl: "js/repository/list/list.component.html"
})
export class ListTemplatesComponent implements OnInit {

    static readonly LOADER = "ListTemplatesComponent.LOADER";

    constructor(private templateService: TemplateService,
                private dataTableService: TdDataTableService,
                private state: StateService) {
    }

    selectedTemplate: TemplateMetadata;
    errorMsg: string = "";
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    /**
     * List of available templates
     */
    public templates: TemplateMetadata[] = [];
    dataSource = new MatTableDataSource();

    public ngOnInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.loadTemplates();
    }

    loadTemplates() {
        this.templateService.getTemplates().subscribe(
            (data: TemplateMetadata[]) => {
                this.templates = data;
                this.dataSource.data = data;
            },
            (error: any) => {
                console.log(error);
                if(error.developerMessage)
                    this.errorMsg += error.developerMessage;

                console.log(this.errorMsg);
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
}
