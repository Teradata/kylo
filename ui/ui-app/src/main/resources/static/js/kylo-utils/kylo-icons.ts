export class KyloIcons {

    static Links ={
        setupGuide:"playlist_play",
        feedActivity:"pages",
        profile:"track_changes",
        lineage:"graphic_eq",
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
        stopped:"blocked",
        flowRate:"tune",
        dateRange:"date_range",
        timeSince:"timer",
        runTime:"alarm",
        numberOfFlowsStarted:"star_border",
        numberOfFlowsFinished:"star",
        jobsList:"library_books",
        info:"details",
        alerts:"warning",
        Actions:{
            clone:"content_copy",
            enable:"play_arrow",
            disable:"pause_outline",
            abandon:"call_made",
            filterHelp:"help",
            uploadFile:"cloud_upload",
            startNow:"play_arrow"
        }
    };

    static getBatchFeedRunStatusIcon(feedHealth:any){
        if(feedHealth.running){
            return KyloIcons.Feed.running
        }
        else {
            return KyloIcons.Feed.stopped
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


}