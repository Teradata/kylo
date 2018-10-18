import * as _ from "underscore"

export class FeedNifiErrorUtil {

    /**
     * parse the errors from the nifiFeed object and populate the errorMap
     * @param nifiFeed
     * @param errorMap
     * @return {number} the number of errors
     */
   public static  parseNifiFeedForErrors(nifiFeed:any, errorMap:any) {
        var count = 0;

        if (nifiFeed != null) {

            if (nifiFeed.errorMessages != null && nifiFeed.errorMessages.length > 0) {
                _.each(nifiFeed.errorMessages,  (msg:any) => {
                    errorMap['FATAL'].push({category: 'General', message: msg});
                    count++;
                })
            }

            if (nifiFeed.feedProcessGroup != null) {
                _.each(nifiFeed.feedProcessGroup.errors,  (processor:any) =>{
                    if (processor.validationErrors) {
                        _.each(processor.validationErrors, (error:any) =>{
                            var copy:any = {};
                            _.extend(copy, error);
                            _.extend(copy, processor);
                            copy.validationErrors = null;
                            errorMap[error.severity].push(copy);
                            count++;
                        });
                    }
                });
            }
            if (errorMap['FATAL'].length == 0) {
                delete errorMap['FATAL'];
            }
            if (errorMap['WARN'].length == 0) {
                delete errorMap['WARN'];
            }
        }
        return count;

    }

}