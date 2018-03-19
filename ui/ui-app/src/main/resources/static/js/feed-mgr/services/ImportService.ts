import * as angular from 'angular';
import * as _ from "underscore";
import {Import} from "./ImportComponentOptionTypes";
import ImportComponentOption = Import.ImportComponentOption;
import IImportComponentType = Import.IImportComponentType;
import ImportProperty = Import.ImportProperty;
import IImportService = Import.IImportService;
import Map = Import.Map;

const moduleName = require('feed-mgr/module-name');


export enum ImportComponentType {
    NIFI_TEMPLATE = IImportComponentType.NIFI_TEMPLATE,
    TEMPLATE_DATA = IImportComponentType.TEMPLATE_DATA,
    FEED_DATA = IImportComponentType.FEED_DATA,
    REUSABLE_TEMPLATE = IImportComponentType.REUSABLE_TEMPLATE,
    REMOTE_INPUT_PORT = IImportComponentType.REMOTE_INPUT_PORT,
    USER_DATASOURCES = IImportComponentType.USER_DATASOURCES,
    TEMPLATE_CONNECTION_INFORMATION = IImportComponentType.TEMPLATE_CONNECTION_INFORMATION
}

export class ImportService implements IImportService{


    private guid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }

    constructor() {

    }

    importComponentTypes(): string[] {
        return Object.keys(ImportComponentType)
    }

    /**
     * return a new component option.
     * Defaults to not overwrite.
     * @param component
     * @return {{importComponent: *, overwriteSelectValue: string, overwrite: boolean, userAcknowledged: boolean, shouldImport: boolean, analyzed: boolean, continueIfExists: boolean, properties: Array}}
     */
    newImportComponentOption(component: IImportComponentType): ImportComponentOption {
        let nameOfType = ImportComponentType[component];
        let option = {importComponent: nameOfType, overwrite: false, userAcknowledged: true, shouldImport: true, analyzed: false, continueIfExists: false, properties: [] as ImportProperty[]}
        return option;
    }

    newReusableTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.REUSABLE_TEMPLATE);
    }

    newTemplateConnectionInfoImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.TEMPLATE_CONNECTION_INFORMATION);
    }

    newTemplateDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.TEMPLATE_DATA);
    }

    newFeedDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.FEED_DATA);
    }

    newRemoteProcessGroupImportOption(): ImportComponentOption {
        let option = this.newImportComponentOption(IImportComponentType.REMOTE_INPUT_PORT);
        option.userAcknowledged = false;
        return option;
    }

    newNiFiTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.NIFI_TEMPLATE);
    }

    newUserDatasourcesImportOption(): ImportComponentOption {
        return this.newImportComponentOption(IImportComponentType.USER_DATASOURCES)
    }

    newUploadKey(): string {
        return _.uniqueId("upload_") + new Date().getTime() + this.guid();
    }

    /**
     * Update properties when a user chooses to overwrite or not
     * @param importComponentOption
     */
    onOverwriteSelectOptionChanged(importComponentOption: ImportComponentOption) {
        importComponentOption.userAcknowledged = true;
        if (importComponentOption.overwriteSelectValue == "true") {
            importComponentOption.overwrite = true;
        }
        else if (importComponentOption.overwriteSelectValue == "false") {
            importComponentOption.overwrite = false;
            importComponentOption.continueIfExists = true;
        }
        else {
            importComponentOption.userAcknowledged = false;
        }
    }

    /**
     * return the map of options as an array ready for upload/import
     * @param importOptionsMap a map of {ImportType: importOption}
     * @returns {Array} the array of options to be imported
     */
    getImportOptionsForUpload(importOptionsMap: Map<ImportComponentOption>): ImportComponentOption[] {
        let importComponentOptions: ImportComponentOption[] = []
        Object.keys(importOptionsMap).forEach((key) => {
            let option = importOptionsMap[key];
            //set defaults for options
            option.errorMessages = [];

            if (option.overwrite) {
                option.userAcknowledged = true;
                option.shouldImport = true;
                option.continueIfExists = true;
            }

            //reset the errors
            option.errorMessages = [];
            importComponentOptions.push(option);
        })
        return importComponentOptions;
    }

    /**
     * Check if an importOption is a specific type
     * @param importOption the option to check
     * @param importComponentType the type of the option
     * @returns {boolean} true if match, false if not
     */
    isImportOption(importOption: ImportComponentOption, importComponentType: IImportComponentType): boolean {
        let nameOfType = ImportComponentType[importComponentType]
        return importOption.importComponent == nameOfType;
    }

}

angular.module(moduleName).factory('ImportService', () => new ImportService());


