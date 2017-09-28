export class WranglerEventType {

    /**
     * Indicates the UI should be refreshed
     */
    static readonly REFRESH = "refresh";

     private constructor() {
         throw new Error("Instances of WranglerEventType cannot be constructed");
     }
}
