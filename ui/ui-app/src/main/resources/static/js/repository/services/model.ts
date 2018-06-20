export class TemplateMetadata {

    fileName: string;
    type: string;
    description: string;
    templateName: string;
    installed: boolean
}

export class ImportStatus{

    fileName: string;
    templateName: string;
    success: boolean;
    valid: boolean;
}