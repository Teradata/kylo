import {Component, Input, OnInit} from "@angular/core";
import {TemplateMetadata} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {IPageChangeEvent} from "@covalent/core/paging";
import {StateService} from "@uirouter/angular";
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

    downloadUrl: string = "/proxy/v1/repository/templates";

    constructor(private templateService: TemplateService,
                private dataTableService: TdDataTableService,
                private state: StateService) {
    }

    selectedTemplate: string;

    dataSource = new MatTableDataSource<TemplateMetadata>(this.filteredTemplates);

    /**
     * List of available templates
     */
    @Input("templates")
    public templates: TemplateMetadata[];

    public ngOnInit() {
        this.filter();
    }

    /**
     * Install template if not already installed
     */
    importTemplates() {
        if (this.selectedTemplate.length == 0) {
            console.warn("Select at least one template to import.")
            return;
        }

        console.log("importing templates: ", this.selectedTemplate);
        this.templateService.importTemplate(this.selectedTemplate)
            .subscribe(data => console.log(data),
                error => console.error(error));
    }

    /*
     * download template from repository.
     */
    downloadTemplate(template: TemplateMetadata) {
        this.templateService.downloadTemplate(template.fileName).subscribe(blob => {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = template.fileName;
            link.click();
        });
    }

    /**
     * select/un-select template to be imported
     */
    toggleImportTemplate(template: TemplateMetadata) {
        this.selectedTemplate = template.fileName;
        let param = {"template": template};
        this.state.go("import-template", param);
    }

    pageSize: number = 50;
    currentPage: number = 1;
    fromRow: number = 1;
    searchTerm: string = '';
    filteredTotal = 0;
    filteredTemplates: TemplateMetadata[] = [];

    page(pagingEvent: IPageChangeEvent): void {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.filter();
    }

    search(searchTerm: string): void {
        this.searchTerm = searchTerm;
        this.filter();
    }

    private filter(): void {
        let newData = this.dataTableService.filterData(this.templates, this.searchTerm, true, []);
        this.filteredTotal = newData.length;
        newData = this.dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredTemplates = newData;
    }
}
