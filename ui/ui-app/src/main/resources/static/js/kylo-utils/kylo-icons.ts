export class KyloIcons {

    static Links ={
        setupGuide:"playlist_play",
        feedActivity:"dashboard",
        profile:"insert_chart_outlined",
        lineage:"device_hub",
        sla:"beenhere",
        versions:"history"
    }

    static Feed = {
        status:"star",
        category:"folder",
        schedule:"date",
        description:"subject",
        tags:"subject",
        running:"directions_run",
        stopped:"pause_circle_outline",
        flowRate:"tune",
        dateRange:"date_range",
        timeSince:"timer",
        runTime:"alarm",
        numberOfFlowsStarted:"star_border",
        numberOfFlowsFinished:"star",
        importFeed:"file_upload",
        Stats: {
            eventSuccessKpi:'offline_pin',
            eventSuccesUndefinedKpi:'remove',
            averageDurationKpi:'access_time',
            flowRateKpi:'tune'
        },
        jobsList:"library_books",
        info:"details",
        alerts:"warning",
        Actions:{
            clone:"content_copy",
            enable:"play_arrow",
            disable:"pause",
            abandon:"call_made",
            filterHelp:"help",
            uploadFile:"cloud_upload",
            startNow:"play_arrow",
            delete: "delete",
            export: "launch"
        }
    };

    static getBatchFeedRunStatusIcon(state:any){
        if(state.running){
            return KyloIcons.Feed.running
        }
        else {
            return KyloIcons.Feed.stopped
        }
    }

    static getBatchFeedRunStatusColor(state:any){
        if(state.running){
            return 'tc-emphasis-1';
        }
        else {
            return 'tc-neutral';
        }
    }

    static getFeedStateColor(state:string){
        if(!state) {
            return "tc-neutral";
        }
        if("DISABLED" == state.toUpperCase()){
            return "tc-caution"
        } else {
            return "tc-neutral"
        }
    }

    static getFeedStateIcon(state:string){
        if(!state) {
            return KyloIcons.Feed.status;
        }

        if("DISABLED" == state.toUpperCase()){
            return KyloIcons.Feed.Actions.disable
        } else if("ENABLED" == state.toUpperCase()){
            return KyloIcons.Feed.Actions.enable;
        } else {
            return KyloIcons.Feed.status;
        }
    }

    static getFeedHealthColor(state:string){
        if (!state) return "tc-neutral";
        if (state == 'UNHEALTHY') {
            return "tc-negative";
        } else {
            return "tc-positive";
        }

    }



}
