import {Component, Input, ViewChild} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Connector} from '../api/models/connector';
import {TdDynamicFormsComponent} from '@covalent/dynamic-forms';
import {FormControl, Validators} from '@angular/forms';
import {UiOption} from '../api/models/ui-option';

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

    @ViewChild(TdDynamicFormsComponent) form: TdDynamicFormsComponent;

    private controls: Map<string, FormControl> = new Map();

    constructor(private state: StateService) {
    }

    public ngOnInit() {
    }

    /**
     * Creates a new datasource for this Connector
     */
    createDatasource() {
        const datasourceId = "ruslans-local-file-system"; //post new datasource, get its id
        this.state.go("catalog.datasource", {datasourceId: datasourceId});
    }

    isInputType(option: UiOption) {
        return option.type === undefined || option.type === '' || option.type === "input" || option.type === "password";
    }

    isSelectType(option: UiOption) {
        return option.type === "select";
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
