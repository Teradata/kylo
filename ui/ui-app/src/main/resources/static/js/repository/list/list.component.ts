import {Component, Input, OnInit} from "@angular/core";
import {TemplateMetadata} from "../services/model";
import {TemplateService} from "../services/template.service";
import {TdDataTableService} from "@covalent/core/data-table";
import {IPageChangeEvent} from "@covalent/core/paging";
import {StateService} from "@uirouter/angular";
import AccessControlService from "../../services/AccessControlService";

/**
 * Displays available datasources
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

    selectedTemplate: string;

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
