import IconUtil from "../../services/icon-util";

export class OpsManagerFeedUtil {


    static decorateFeedSummary =  (feed: any) =>{
        //GROUP FOR FAILED

        if (feed.isEmpty == undefined) {
            feed.isEmpty = false;
        }

        var health: string = "---";
        if (!feed.isEmpty) {
            health = feed.healthy ? 'HEALTHY' : 'UNHEALTHY';
            var iconData: any = IconUtil.iconForFeedHealth(health);
            feed.icon = iconData.icon
            feed.iconstyle = iconData.style
        }
        feed.healthText = health;
        if (feed.running) {
            feed.displayStatus = 'RUNNING';
        }
        else if ("FAILED" == feed.lastStatus || ( "FAILED" == feed.lastExitCode && "ABANDONED" != feed.lastStatus)) {
            feed.displayStatus = 'FAILED';
        }
        else if ("COMPLETED" == feed.lastExitCode) {
            feed.displayStatus = 'COMPLETED';
        }
        else if ("STOPPED" == feed.lastStatus) {
            feed.displayStatus = 'STOPPED';
        }
        else if ("UNKNOWN" == feed.lastStatus) {
            feed.displayStatus = 'INITIAL';
            feed.sinceTimeString = '--';
            feed.runTimeString = "--"
        }
        else {
            feed.displayStatus = feed.lastStatus;
        }

        feed.statusStyle = IconUtil.iconStyleForJobStatus(feed.displayStatus);
    }
}