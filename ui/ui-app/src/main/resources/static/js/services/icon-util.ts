export default class IconUtil {

    constructor() { }

    static iconForFeedHealth(health: any) {
        return IconUtil.iconForHealth(health);
    }
    static iconForHealth(health: any) {
        var data = { icon: "help", style: 'unknown', color: '#eee', text: 'UNKNOWN', class: '' };
        switch (health) {
            case "HEALTHY":
            case "UP":
            case "OK":
                data.icon = 'mood';
                data.style = 'healthy';
                data.color = "#009933";
                data.class = "tc-positive";
                data.text = "HEALTHY"
                break;
            case "UNHEALTHY":
            case "DOWN":
            case "CRITICAL":
            case "ERROR":
                data.icon = 'mood_bad';
                data.style = 'unhealthy';
                data.color = "#FF0000";
                data.text = "UNHEALTHY"
                break;
            case "WARNING":
                data.icon = 'warning';
                data.style = 'warn';
                data.color = "#FF9901";
                data.text = "WARNING"
                break;
            case "UNKNOWN":
                data.icon = "help";
                data.style = "unknown";
                data.color = "#969696";
                data.text = "UNKNOWN"
        }
        return data;
    }

   static  iconForServiceComponentAlert(health: any) {
        var data = { icon: "help", style: 'unknown', color: '#969696', text: 'UNKNOWN' };
        switch (health) {
            case "OK":
                data.icon = "check_circle";
                data.style = "success";
                data.color = "#009933";
                data.text = "OK"
                break;
            case "WARNING":
                data.icon = 'warning';
                data.style = 'warn';
                data.color = "#FF9901";
                data.text = "WARNING"
            case "ERROR":
            case "CRITICAL":
                data.icon = 'mood_bad';
                data.style = 'unhealthy';
                data.color = "#FF0000";
                data.text = "CRITICAL"
                break;
            case "UNKNOWN":
                data.icon = "help";
                data.style = "unknown";
                data.color = "#969696";
                data.text = "UNKNOWN"
                break;
        }
        return data;
    }

   static iconDataForJobStatus(status: any) {
        var data = { icon: "help", style: 'unknown', color: '#969696' };
        switch (status) {
            case "FAILED":
                data.icon = 'error_outline';
                data.style = 'error';
                data.color = "#FF0000";
                break;
            case "COMPLETED":
                data.icon = "check_circle";
                data.style = "success";
                data.color = "#009933";
                break;
            case "ABANDONED":
                data.icon = "call_made";
                data.style = "abandoned";
                data.color = "#969696";
                break;
            case "RUNNING_JOB_ACTIVITY":
                data.icon = "directions_run";
                data.style = "running-job-activity";
                data.color = "#00b2b1"; //teal color
                break;
            case "RUNNING":
            case "EXECUTING":
            case "STARTED":
            case "STARTING":
                data.icon = "directions_run";
                data.style = "success";
                data.color = "#00b2b1";  //teal color
                break;
            case "STOPPING":
                data.icon = "pan_tool";
                data.style = "warn";
                data.color = "#FF9901";
            case "STOPPED":
                data.icon = "pan_tool";
                data.style = "warn";
                data.color = "#FF9901";
                break;
            case "WARNING":
                data.icon = "warning";
                data.style = "warn";
                data.color = "#FF9901";
                break;
            case "UNKNOWN":
            case "INITIAL":
                data.icon = "help";
                data.style = "unknown";
                data.color = "#969696";
                break;
            default:
                console.error("unknown icon!!!!!!!!!!");
        }
        return data;
    }

  static iconForJobStatus(status: any) {
        var iconData = IconUtil.iconDataForJobStatus(status);
        return iconData.icon;
    }

   static iconStyleForJobStatus(status: any) {
        var iconData = IconUtil.iconDataForJobStatus(status);
        return iconData.style;
    }

   static colorForJobStatus(status: any) {
        var iconData = IconUtil.iconDataForJobStatus(status);
        return iconData.color;
    }


}
