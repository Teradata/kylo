export interface DataSetTemplate {

    files?: string[];

    format?: string;

    jars?: string[];

    options?: { [k: string]: string };

    paths?: string[];
}
