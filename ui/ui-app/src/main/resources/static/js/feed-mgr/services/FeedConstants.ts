import * as _ from 'underscore';

export class FeedConstants {

    static DEFAULT_CRON:string = "0 0 12 1/1 * ? *";


    static SCHEDULE_STRATEGIES:any = {
    TRIGGER_DRIVEN:{label: 'Trigger/Event', value: "TRIGGER_DRIVEN"},
    TIMER_DRIVEN:{label: 'Timer', value: "TIMER_DRIVEN"},
    PRIMARY_NODE_ONLY:{label: "On primary node", value: "PRIMARY_NODE_ONLY"},
    CRON_DRIVEN:{label: 'Cron', value: "CRON_DRIVEN"}
    };

    static scheduleStrategyLabel(strategyKey:string){
        if(strategyKey) {
            let strategy = Object.keys(this.SCHEDULE_STRATEGIES).find((key: string) => key == strategyKey);
            if (strategy) {
                return this.SCHEDULE_STRATEGIES[strategy].label;
            }
            else {
                return strategyKey;
            }
        }
        else {
            return '';
        }
    }

    /**
     * In the Data Processing section these are the available Strategies a user can choose when defining the feed
     */
   static mergeStrategies:any= [
        { name: 'Sync', type: 'SYNC', hint: 'Replace table content', disabled: false },
        { name: 'Rolling sync', type: 'ROLLING_SYNC', hint: 'Replace content in matching partitions' },
        { name: 'Merge', type: 'MERGE', hint: 'Insert all rows', disabled: false },
        { name: 'Dedupe and merge', type: 'DEDUPE_AND_MERGE', hint: 'Insert rows ignoring duplicates', disabled: false },
        { name: 'Merge using primary key', type: 'PK_MERGE', hint: 'Upsert using primary key' }
    ];

    /**
     * The available Target Format options
     */
   static targetFormatOptions:any= [{ label: "ORC", value: 'STORED AS ORC' },
        { label: "PARQUET", value: 'STORED AS PARQUET' },
        { label: "AVRO", value: 'STORED AS AVRO' },
        {
            label: "TEXTFILE",
            value: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
            + ' STORED AS'
            + ' TEXTFILE'
        },
        { label: "RCFILE", value: 'ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe" STORED AS RCFILE' }];
    /**
     * The available Compression options for a given targetFormat {@see this#targetFormatOptions}
     */
    static compressionOptions:any= { "ORC": ['NONE', 'SNAPPY', 'ZLIB'], "PARQUET": ['NONE', 'SNAPPY'], "AVRO": ['NONE'] };

    /**
     * Standard data types for column definitions
     */
    static columnDefinitionDataTypes:any= ['string', 'int', 'bigint', 'tinyint', 'decimal', 'double', 'float', 'date', 'timestamp', 'boolean', 'binary'];

    /**
     * Returns an array of all the compression options regardless of the {@code targetFormat}
     * (i.e. ['NONE','SNAPPY','ZLIB']
     * @returns {Array}
     */
   static allCompressionOptions() {
        let arr: any[] = [];
        _.each(FeedConstants.compressionOptions, (options: any) => {
            arr = _.union(arr, options);
        });
        return arr;
    };
}