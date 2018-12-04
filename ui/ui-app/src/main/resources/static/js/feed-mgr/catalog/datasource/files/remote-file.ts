import {DatePipe} from '@angular/common';
import {TdBytesPipe} from '@covalent/core/common';
import {BrowserObject} from '../../api/models/browser-object';
import {BrowserColumn} from '../../api/models/browser-column';

export class RemoteFile extends BrowserObject {
    directory: boolean;
    length: number;
    modificationTime: Date;
    path: string;

    constructor(name: string, path: string, directory: boolean, length: number, modificationTime: Date) {
        super();
        this.name = name;
        this.path = path;
        this.directory = directory;
        this.length = ( length == null ? 0 : length);
        this.modificationTime = modificationTime;
    }

    canBeParent(): boolean {
        return this.directory;
    }

    getIcon() {
        return this.directory ? 'fa-folder' : 'fa-file-r'
    }

    getPath(): string {
        return this.path;
    }
}

export class RemoteFileDescriptor {

    static FILE_SIZE_FORMAT: (v: any) => any = (v: number) => new TdBytesPipe().transform(v, 0);
    static DATE_FORMAT: (v: any) => any = (v: number) => new DatePipe('en-US').transform(v, 'yyyy-MM-dd hh:mm:ss');

    static COLUMNS: BrowserColumn[] = [
        {name: "selection", label: " ", sortable: false, width: 40, filter: false, icon: false},
        {name: "type", label: " ", sortable: false, width: 40, filter: false, icon: true},
        {name: "name", label: "Name", sortable: true, filter: true},
        {name: "length", label: "Size", numeric: true, sortable: true, filter: false, width: 200, format: RemoteFileDescriptor.FILE_SIZE_FORMAT},
        {name: "modificationTime", label: "Last modified", sortable: true, filter: false, width: 210, format: RemoteFileDescriptor.DATE_FORMAT}
    ];
}