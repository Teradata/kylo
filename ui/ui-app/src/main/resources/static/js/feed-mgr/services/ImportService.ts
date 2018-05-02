import * as angular from 'angular';
import * as _ from "underscore";
import {Import} from "./ImportComponentOptionTypes";
import {Common} from "../../common/CommonTypes";
import ImportComponentOption = Import.ImportComponentOption;
import ImportProperty = Import.ImportProperty;
import ImportService = Import.ImportService;
import Map = Common.Map;

const moduleName = require('feed-mgr/module-name');


export enum ImportComponentType {
    NIFI_TEMPLATE = Import.ImportComponentType.NIFI_TEMPLATE,
    TEMPLATE_DATA = Import.ImportComponentType.TEMPLATE_DATA,
    FEED_DATA =  Import.ImportComponentType.FEED_DATA,
    REUSABLE_TEMPLATE =  Import.ImportComponentType.REUSABLE_TEMPLATE,
    REMOTE_INPUT_PORT =  Import.ImportComponentType.REMOTE_INPUT_PORT,
    USER_DATASOURCES =  Import.ImportComponentType.USER_DATASOURCES,
    TEMPLATE_CONNECTION_INFORMATION =  Import.ImportComponentType.TEMPLATE_CONNECTION_INFORMATION,
    FEED_CATEGORY_USER_FIELDS = Import.ImportComponentType.FEED_CATEGORY_USER_FIELDS,
    FEED_USER_FIELDS = Import.ImportComponentType.FEED_USER_FIELDS
}

export class DefaultImportService implements ImportService{


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
    newImportComponentOption(component:  Import.ImportComponentType): ImportComponentOption {
        let nameOfType = component;
        let option = {importComponent: nameOfType, overwrite: false, userAcknowledged: true, shouldImport: true, analyzed: false, continueIfExists: false, properties: [] as ImportProperty[]}
        return option;
    }

    newReusableTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.REUSABLE_TEMPLATE);
    }

    newTemplateConnectionInfoImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.TEMPLATE_CONNECTION_INFORMATION);
    }

    newTemplateDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.TEMPLATE_DATA);
    }

    newFeedDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.FEED_DATA);
    }

    newRemoteProcessGroupImportOption(): ImportComponentOption {
        let option = this.newImportComponentOption( Import.ImportComponentType.REMOTE_INPUT_PORT);
        option.shouldImport = false;
        option.userAcknowledged = false;
        return option;
    }

    newNiFiTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.NIFI_TEMPLATE);
    }

    newUserDatasourcesImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.USER_DATASOURCES)
    }
    
    newFeedCategoryUserFieldsImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.FEED_CATEGORY_USER_FIELDS);
    }

    newFeedUserFieldsImportOption(): ImportComponentOption {
        return this.newImportComponentOption( Import.ImportComponentType.FEED_USER_FIELDS);
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
    isImportOption(importOption: ImportComponentOption, importComponentType:  Import.ImportComponentType): boolean {
        let nameOfType = importComponentType
        return importOption.importComponent == nameOfType;
    }

}

angular.module(moduleName).factory('ImportService', () => new DefaultImportService());


