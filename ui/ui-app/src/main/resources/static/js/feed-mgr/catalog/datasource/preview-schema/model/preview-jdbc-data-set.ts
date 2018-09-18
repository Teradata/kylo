import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {PreviewDataSet} from "./preview-data-set";
import {PreviewDataSetRequest} from "./preview-data-set-request";

/**
 * JDBC Data Set
 */
export class PreviewJdbcDataSet extends PreviewDataSet {

    public constructor(init?: Partial<PreviewJdbcDataSet>) {
        super(init);
        Object.assign(this, init);
        this.type = "JDBCDataSet"
    }

    public updateDisplayKey() {
        this.displayKey = this.getPreviewItemPath()
    }

    // TODO: use qualifiedIdentifier from REST API instead of reconstructing a different one
    public getPreviewItemPath(): string {
        let path = "";
        if (this.items && this.items.length > 0) {
            path = this.items[0]
        }
        if(path == "") {
            const itemMap = this.getItemMap();
            if (itemMap["name"]) {
                const schema = itemMap["catalog"] != undefined ? itemMap["catalog"] : itemMap["schema"];
                path = schema + "." + itemMap["name"];
            } else {
                //error
                path = "";
            }
        }
        return path;
    }

    public applyPreviewRequestProperties(previewRequest: PreviewDataSetRequest) {
        super.applyPreviewRequestProperties(previewRequest);
        previewRequest.properties = {};
        previewRequest.properties.dbtable = previewRequest.previewItem;
    }

    public toSparkDataSet(): SparkDataSet {
        let sparkDataSet = super.toSparkDataSet();
        sparkDataSet.options['dbtable'] = this.getPreviewItemPath();
        sparkDataSet.format = "jdbc";

        // Set database name for PostgreSQL data sources
        const url = PreviewJdbcDataSet.getOption("url", sparkDataSet);
        if (url != null && url.startsWith("jdbc:postgres:")) {
            sparkDataSet.options["PGDBNAME"] = this.getItemMap()["catalog"];
        }

        return sparkDataSet;
    }

    private getItemMap(): { [k: string]: string } {
        if (this.items && this.items.length > 0) {
            const itemMap = {};
            this.items[0].split("&").forEach((v: string) => {
                let arr = v.split("=");
                itemMap[arr[0]] = arr[1];
            });
            return itemMap;
        } else {
            //error
            return {};
        }
    }

    /**
     * Returns the value for the specified option.
     */
    private static getOption(name: string, dataSet: SparkDataSet): string {
        if (dataSet.options && dataSet.options[name]) {
            return dataSet.options[name];
        }

        const dataSource = dataSet.dataSource;
        if (dataSource) {
            if (dataSource.template && dataSource.template.options && dataSource.template.options[name]) {
                return dataSource.template.options[name];
            }

            const connector = dataSource.connector;
            if (connector && connector.template && connector.template.options && connector.template.options[name]) {
                return connector.template.options[name];
            }
        }

        return null;
    }
}
