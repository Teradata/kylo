export class Sla {
    id: string;
    name: string;
    description: string;
    feedNames: string;
    rules: Array<any> = [];
    canEdit: boolean = false;
    actionConfigurations: Array<any> = [];
    actionErrors: Array<any> = [];
    editable: boolean = false;
}