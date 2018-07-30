/**
 * NiFi cluster status
 */

/*
    Sample value

    {
        "version": "1.6.0",
        "clustered": false
    }
*/
export class NiFiClusterStatus {

    /**
     * NiFi version
     */
    version: string;

    /**
     * Is NiFi running in clustered mode?
     */
    clustered: boolean;

    constructor(p_version: string, p_clustered: boolean) {
        this.version = p_version;
        this.clustered = p_clustered;
    }
}