import {DatePipe} from '@angular/common';
import {TdBytesPipe} from '@covalent/core/common';
import {ITdDataTableColumn} from '@covalent/core/data-table';

export interface RemoteFile {
    name: string;
    directory: boolean;
    length: number;
    modificationTime: Date;
    path: string;
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