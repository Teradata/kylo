
export class DateRange {

    constructor(public startTime:number, public endTime:number) {}
}
export class FeedStats {


        lastRefreshTime: number;
        time:DateRange = new DateRange(0,0);
        flowsStartedPerSecond: number = 0;
        flowsStarted: number = 0;
        flowsFinished = 0;
        flowDuration= 0;
        flowsFailed =0;
        totalEvents = 0;
        failedEvents = 0;
        flowsSuccess = 0;
        flowsRunning =0;
        avgFlowDuration = 0;
        avgFlowDurationMilis = 0;
        constructor() {

        }


}