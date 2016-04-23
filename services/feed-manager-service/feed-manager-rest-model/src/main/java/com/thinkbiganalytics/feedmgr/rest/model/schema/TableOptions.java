package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 2/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableOptions {
    private boolean compress; ///format
    private boolean auditLogging;
    private boolean encrypt; // encrypt zone
    private boolean trackHistory;

    private String compressionFormat;

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isAuditLogging() {
        return auditLogging;
    }

    public void setAuditLogging(boolean auditLogging) {
        this.auditLogging = auditLogging;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isTrackHistory() {
        return trackHistory;
    }

    public void setTrackHistory(boolean trackHistory) {
        this.trackHistory = trackHistory;
    }

    public String getCompressionFormat() {
        return compressionFormat;
    }

    public void setCompressionFormat(String compressionFormat) {
        this.compressionFormat = compressionFormat;
    }
}
