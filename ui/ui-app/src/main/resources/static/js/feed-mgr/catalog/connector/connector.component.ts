import {Component, Input} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Connector} from '../api/models/connector';
import {FormControl, Validators} from '@angular/forms';
import {UiOption} from '../api/models/ui-option';
import {DataSource} from '../api/models/datasource';
import {DataSourceTemplate} from '../api/models/datasource-template';

/**
 * Displays selected connector properties.
 */
@Component({
    selector: "catalog-connector",
    styleUrls: ["js/feed-mgr/catalog/connector/connector.component.css"],
    templateUrl: "js/feed-mgr/catalog/connector/connector.component.html"
})
export class ConnectorComponent {

    static LOADER = "ConnectorComponent.LOADER";

    @Input("connector")
    public connector: Connector;

    private titleControl: FormControl;
    private controls: Map<string, FormControl> = new Map();

    constructor(private state: StateService) {
        this.titleControl = new FormControl('', Validators.required);
    }

    public ngOnInit() {
    }

    /**
     * Creates a new datasource for this Connector
     */
    createDatasource() {
        const ds = new DataSource();
        ds.title = this.titleControl.value;
        ds.connector = this.connector;
        ds.template = new DataSourceTemplate();
        ds.template.paths = [];
        ds.template.options = {};
        if (this.connector.optionsMapperId === "azure") {

        } else {
            this.controls.forEach((value: FormControl, key: string) => {
                if (key === "path") {
                    ds.template.paths.push(this.controls.get(key).value);
                } else {
                    ds.template.options[key] = this.controls.get(key).value;
                }
            });
        }

        const datasourceId = "";
        this.state.go("catalog.datasource", {datasourceId: datasourceId});
    }

    isInputType(option: UiOption): boolean {
        return option.type === undefined || option.type === '' || option.type === "input" || option.type === "password";
    }

    isSelectType(option: UiOption): boolean {
        return option.type === "select";
    }

    isFormInvalid(): boolean {
        return this.titleControl.invalid || Array.from(this.controls.values())
            .map(item => item.invalid)
            .reduce((prev, current) => {
                    return prev || current;
                }, false
            );
    }

    getControl(option: UiOption) {
        let control = this.controls.get(option.key);
        if (!control) {
            const validators = [];
            if (option.required) {
                validators.push(Validators.required);
            }
            control = new FormControl('', validators);
            if (option.value) {
                control.setValue(option.value);
            }
            this.controls.set(option.key, control);
        }
        return control;
    }
}
