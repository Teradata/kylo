package com.thinkbiganalytics.jobrepo.query.model;

import java.io.Serializable;

public class ExitStatus implements Serializable, Comparable<ExitStatus> {
    public static final ExitStatus UNKNOWN = new ExitStatus("UNKNOWN");
    public static final ExitStatus EXECUTING = new ExitStatus("EXECUTING");
    public static final ExitStatus COMPLETED = new ExitStatus("COMPLETED");
    public static final ExitStatus NOOP = new ExitStatus("NOOP");
    public static final ExitStatus FAILED = new ExitStatus("FAILED");
    public static final ExitStatus STOPPED = new ExitStatus("STOPPED");
    private final String exitCode;
    private final String exitDescription;

    public ExitStatus(String exitCode) {
        this(exitCode, "");
    }

    public ExitStatus(String exitCode, String exitDescription) {
        this.exitCode = exitCode;
        this.exitDescription = exitDescription == null?"":exitDescription;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getExitDescription() {
        return exitDescription;
    }

    public String toString() {
        return String.format("exitCode=%s;exitDescription=%s", new Object[]{this.exitCode, this.exitDescription});
    }

    public boolean equals(Object obj) {
        return obj == null?false:this.toString().equals(obj.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    private int severity(ExitStatus status) {
        return status.exitCode.startsWith(EXECUTING.exitCode)?1:(status.exitCode.startsWith(COMPLETED.exitCode)?2:(status.exitCode.startsWith(NOOP.exitCode)?3:(status.exitCode.startsWith(STOPPED.exitCode)?4:(status.exitCode.startsWith(FAILED.exitCode)?5:(status.exitCode.startsWith(UNKNOWN.exitCode)?6:7)))));
    }

    public int compareTo(ExitStatus status) {
        return this.severity(status) > this.severity(this)?-1:(this.severity(status) < this.severity(this)?1:this.getExitCode().compareTo(status.getExitCode()));
    }

}
