import {Component, Input} from "@angular/core";
import {TemplateMetadata} from "../api/model/model";
import {TemplateService} from "../services/template.service";

/**
 * Displays available datasources
 */
@Component({
    selector: "list-templates",
    templateUrl: "js/marketplace/templates/list/list.component.html"
})
export class ListTemplatesComponent {

    static readonly LOADER = "ListTemplatesComponent.LOADER";

    constructor(private templateService: TemplateService){}

    selectedTemplates: string[] = [];

    /**
     * List of available templates
     */
    @Input("templates")
    public templates: TemplateMetadata[];

    /**
     * Install template if not already installed
     */
    importTemplates(){
        if(this.selectedTemplates.length == 0){
            console.warn("Select at least one template to import.")
            return;
        }

        console.log("importing templates: ", this.selectedTemplates);
        this.templateService.importTemplate(this.selectedTemplates)
            .subscribe(data => console.log(data),
                error => console.error(error));
    }

    /**
     * select/un-select template to be imported
     */
    toggleImportTemplate(event: any, template: TemplateMetadata){
        if(event.checked)
            this.selectedTemplates.push(template.fileName);
        else {
            for (let i = 0; i < this.selectedTemplates.length; i++){
                this.selectedTemplates.splice(i, 1);
                break;
            }
        }
        console.log(this.selectedTemplates);
    }
}
