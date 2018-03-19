import * as angular from "angular";
import * as _ from "underscore";
 declare namespace Import {

     export const enum IImportComponentType {
        NIFI_TEMPLATE, TEMPLATE_DATA, FEED_DATA, REUSABLE_TEMPLATE, REMOTE_INPUT_PORT, USER_DATASOURCES, TEMPLATE_CONNECTION_INFORMATION
    }

     interface Collection<T> { }

     export interface Map<T> extends Collection<T>{
        [K: string]: T;
    }

    export interface ImportProperty {

        processorName: string;
        processorId: string;
        processorType: string;
        propertyKey: string;
        propertyValue: string;
    }

     export interface InputPortListItem {
        label: string;
        value: string;
        description: string;
        disabled: boolean;
    }

    /**
     * Defines the domain type (zip, phone, credit card) of a column.
     */
    export interface ImportComponentOption {

        importComponent: string;

        /**
         * Should we overwrite this data when importing if it already exists
         * TODO can we delete
         */
        overwriteSelectValue?: string;

        /**
         * Should we overwrite this data when importing if it already exists
         */
        overwrite: boolean;

        /**
         * Has the user explicitly checked they acknowledge the settings before importing
         */
        userAcknowledged: boolean;

        /**
         * Should this component be imported.  If false it will be skipped
         */
        shouldImport: boolean;

        /**
         * TODO can we delete
         */
        analyzed: boolean;

        /**
         * TODO can we delete
         */
        continueIfExists: boolean;

        /**
         * Array of properties
         */
        properties: ImportProperty[];

        /**
         *
         */
        errorMessages?: any[];

        /**
         * connection info to connect reusable templates together
         */
        connectionInfo?:ReusableTemplateConnectionInfo[];

        /**
         * information about what ports should be created as 'remote input ports'
         */
        remoteProcessGroupInputPorts?:RemoteProcessInputPort[];

    }


     export interface IImportService {
        /**
         * return a new component option.
         * Defaults to not overwrite.
         * @param component
         * @return {{importComponent: *, overwriteSelectValue: string, overwrite: boolean, userAcknowledged: boolean, shouldImport: boolean, analyzed: boolean, continueIfExists: boolean, properties: Array}}
         */
        newImportComponentOption(component: IImportComponentType): ImportComponentOption;

        newReusableTemplateImportOption(): ImportComponentOption;

        newTemplateConnectionInfoImportOption(): ImportComponentOption;

        newTemplateDataImportOption(): ImportComponentOption;

        newFeedDataImportOption(): ImportComponentOption;

        newRemoteProcessGroupImportOption(): ImportComponentOption;

        newNiFiTemplateImportOption(): ImportComponentOption;

        newUserDatasourcesImportOption(): ImportComponentOption;

        newUploadKey(): string;

        /**
         * Update properties when a user chooses to overwrite or not
         * @param importComponentOption
         */
        onOverwriteSelectOptionChanged(importComponentOption: ImportComponentOption): void;

        /**
         * return the map of options as an array ready for upload/import
         * @param importOptionsMap a map of {ImportType: importOption}
         * @returns {Array} the array of options to be imported
         */
        getImportOptionsForUpload(importOptionsMap: Map<ImportComponentOption>): ImportComponentOption[];

        /**
         * Check if an importOption is a specific type
         * @param importOption the option to check
         * @param importComponentType the type of the option
         * @returns {boolean} true if match, false if not
         */
        isImportOption(importOption: ImportComponentOption, importComponentType: IImportComponentType): boolean;
    }


     export interface ProcessGroupEntity {
        id: string;
        parentGroupId: string;
        name: string;
        comments: string;
    }

     export interface InputProcessor {
        id: string;
        parentGroupId: string;
        name: string;
        type: string;
        state: string;
    }

     export interface GeneralError {
        message: string;
        category?: any;
        severity: string;
    }

     export interface ComponentError {
        processorName: string;
        processorId: string;
        processGroupName?: any;
        processGroupId: string;
        validationErrors: GeneralError[];
    }

     export interface RemoteProcessInputPort {
        templateName: string;
        inputPortName: string;
        selected: boolean;
        existing: boolean;
    }

     export interface ReusableTemplateConnectionInfo {
        feedOutputPortName: string;
        reusableTemplateInputPortName: string;
        inputPortDisplayName: string;
        reusableTemplateProcessGroupName: string;
    }

     export  interface TemplateResults {
        processGroupEntity: ProcessGroupEntity;
        inputProcessor: InputProcessor;
        success: boolean;
        errors: ComponentError[];
        rolledBack: boolean;
        controllerServiceErrors: any[];
        allErrors: GeneralError[];
    }

     export interface ImportOptions {
        uploadKey: string;
        importComponentOptions: ImportComponentOption[];
    }


     export interface ImportTemplateResult {
        fileName: string;
        templateName: string;
        success: boolean;
        valid: boolean;
        templateResults: TemplateResults;
        controllerServiceErrors: any[];
        templateId?: any;
        nifiTemplateId: string;
        zipFile: boolean;
        nifiTemplateXml: string;
        templateJson?: any;
        nifiConnectingReusableTemplateXmls: any[];
        verificationToReplaceConnectingResuableTemplateNeeded: boolean;
        reusableFlowOutputPortConnectionsNeeded: boolean;
        remoteProcessGroupInputPortsNeeded: boolean;
        remoteProcessGroupInputPortNames: RemoteProcessInputPort[];
        reusableTemplateConnections: ReusableTemplateConnectionInfo[];
        importOptions: ImportOptions;
    }
}