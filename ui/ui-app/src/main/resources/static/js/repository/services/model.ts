export class TemplateRepository {
    name: string;
    type: string;
    location: string;
    readOnly: boolean;
}

export class TemplateMetadata {

    fileName: string;
    type: string;
    description: string;
    templateName: string;
    installed: boolean;
    updateAvailable: boolean;
    repository: TemplateRepository;
    updates: any[];
    lastModified: any;
}

export class ImportStatus{

    fileName: string;
    templateName: string;
    success: boolean;
    valid: boolean;
}