///<reference path="ImportComponentOptionTypes.ts"/>
import * as angular from 'angular';
import * as _ from "underscore";
import {Common} from '../../../lib/common/CommonTypes';
import Map = Common.Map;
import {moduleName} from "../module-name";
import {ImportComponentOption, ImportComponentType, ImportProperty, ImportService} from './ImportComponentOptionTypes';


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

    /**
     * return a new component option.
     * Defaults to not overwrite.
     * @param component
     * @return {{importComponent: *, overwriteSelectValue: string, overwrite: boolean, userAcknowledged: boolean, shouldImport: boolean, analyzed: boolean, continueIfExists: boolean, properties: Array}}
     */
    newImportComponentOption(component:  ImportComponentType): ImportComponentOption {
        let nameOfType = component;
        let option = {importComponent: nameOfType, overwrite: false, userAcknowledged: true, shouldImport: true, analyzed: false, continueIfExists: false, properties: [] as ImportProperty[]}
        return option;
    }

    newReusableTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.REUSABLE_TEMPLATE);
    }

    newTemplateConnectionInfoImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.TEMPLATE_CONNECTION_INFORMATION);
    }

    newTemplateDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.TEMPLATE_DATA);
    }

    newFeedDataImportOption(): ImportComponentOption {
        return this.newImportComponentOption(ImportComponentType.FEED_DATA);
    }

    newUserDataSetsImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.USER_DATA_SETS);
    }

    newRemoteProcessGroupImportOption(): ImportComponentOption {
        let option = this.newImportComponentOption( ImportComponentType.REMOTE_INPUT_PORT);
        option.shouldImport = false;
        option.userAcknowledged = false;
        return option;
    }

    newNiFiTemplateImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.NIFI_TEMPLATE);
    }

    newUserDatasourcesImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.USER_DATASOURCES)
    }
    
    newFeedCategoryUserFieldsImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.FEED_CATEGORY_USER_FIELDS);
    }

    newFeedUserFieldsImportOption(): ImportComponentOption {
        return this.newImportComponentOption( ImportComponentType.FEED_USER_FIELDS);
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
    isImportOption(importOption: ImportComponentOption, importComponentType:  ImportComponentType): boolean {
        let nameOfType = importComponentType
        return importOption.importComponent == nameOfType;
    }

}

angular.module(moduleName).factory('ImportService', () => new DefaultImportService());


