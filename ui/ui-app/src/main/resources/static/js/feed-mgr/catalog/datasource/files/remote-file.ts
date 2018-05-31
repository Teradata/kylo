import {DatePipe} from '@angular/common';
import {TdBytesPipe} from '@covalent/core/common';
import {ITdDataTableColumn} from '@covalent/core/data-table';
import {BrowserObject} from '../api/browser-object';

export class RemoteFile extends BrowserObject {
    directory: boolean;
    length: number;
    modificationTime: Date;

    constructor(name: string, path: string, directory: boolean, length: number, modificationTime: Date) {
        super();
        this.name = name;
        this.path = path;
        this.directory = directory;
        this.length = length;
        this.modificationTime = modificationTime;
    }

    canBeParent(): boolean {
        return this.directory;
    }
}

export class RemoteFileDescriptor {

    static FILE_SIZE_FORMAT: (v: any) => any = (v: number) => new TdBytesPipe().transform(v, 2);
    static DATE_FORMAT: (v: any) => any = (v: number) => new DatePipe('en-US').transform(v, 'dd/MM/yyyy hh:mm:ss');

    static COLUMNS: ITdDataTableColumn[] = [
        {name: "directory", label: "", sortable: false, width: 48, filter: false},
        {name: "name", label: "Name", sortable: true, filter: true},
        {name: "length", label: "Size", numeric: true, sortable: true, filter: false, width: 200, format: RemoteFileDescriptor.FILE_SIZE_FORMAT},
        {name: "modificationTime", label: "Last modified", sortable: true, filter: false, width: 210, format: RemoteFileDescriptor.DATE_FORMAT}
    ];
}