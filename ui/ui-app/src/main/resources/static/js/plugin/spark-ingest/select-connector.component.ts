import {Component, Input} from "@angular/core";
import {TdMediaService} from "@covalent/core/media";

@Component({
    styleUrls: ["js/plugin/spark-ingest/select-connector.component.css"],
    templateUrl: "js/plugin/spark-ingest/select-connector.component.html"
})
export class SelectConnectorComponent {

    @Input()
    public model: any;

    @Input()
    public stepIndex: string;

    @Input()
    public stepper: any;

    items: object[] = [{
        color: "deep-purple-A700",
        icon: "storage",
        title: "SQL Source",
        sparkFormat: "jdbc"
    }, {
        color: "blue-A700",
        icon: "amazon",
        title: "Amazon S3"
    }, {
        color: "pink-A700",
        icon: "fast_forward",
        title: "Kafka",
        sparkFormat: "org.apache.spark.sql.kafka010.KafkaSourceProvider",
        sparkJars: "file:/opt/nifi/mysql/kafka-clients-0.10.0.1.jar;file:/opt/nifi/mysql/spark-sql-kafka-0-10_2.11-2.2.0.jar",
        sparkOptions: "kafka.bootstrap.servers=localhost:9092"
    }, {
        color: "cyan-A700",
        icon: "file_upload",
        title: "File Upload"
    }, {
        color: "deep-orange-A700",
        icon: "cloud",
        title: "HDFS"
    }, {
        color: "lime-A700",
        icon: "storage",
        title: "Teradata",
        sparkFormat: "jdbc",
        sparkJars: "file:/opt/nifi/mysql/tdgssconfig.jar;file:/opt/nifi/mysql/terajdbc4.jar",
        sparkOptions: "driver=com.teradata.jdbc.TeraDriver;url=jdbc:teradata://34.211.12.96;user=dbc;password=0Teradata$"
    }, {
        color: "amber-A700",
        icon: "windows",
        title: "Google Cloud Storage",
        sparkJars: "file:/opt/nifi/mysql/gcs-connector-latest-hadoop2.jar",
        sparkOptions: "spark.hadoop.google.cloud.auth.service.account.email=drive-952@freastro.iam.gserviceaccount.com;spark.hadoop.google.cloud.auth.service.account.keyfile=/opt/nifi/mysql/freastro-e330d4b6b80b.p12"
    }, {
        color: "green-A700",
        icon: "storage",
        title: "Hive"
    }];

    constructor(public media: TdMediaService) {
    }

    selectConnector(connector: object) {
        this.model.tableOption.sparkConnector = connector;

        // Go to next step
        const step = this.stepper.getStep(this.stepIndex);
        this.stepper.stepEnabled(step.nextActiveStepIndex);
        this.stepper.completeStep(step.index);
        window.setTimeout(() => this.stepper.selectedStepIndex = step.nextActiveStepIndex, 0);
    }
}
