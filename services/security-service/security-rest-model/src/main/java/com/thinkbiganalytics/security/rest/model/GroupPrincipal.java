package com.thinkbiganalytics.security.rest.model;

public class GroupPrincipal {

    /** A human-readable summary */
    private String description;

    /** Number of users and groups within this group */
    private int memberCount;

    /** Unique name */
    private String systemName;

    /** Human-readable name */
    private String title;

    /**
     * Gets a human-readable description of this group.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a human-readable description of this group.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the number of users and groups contained within this group.
     *
     * @return the member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Sets the number of users and groups contained within this group.
     *
     * @param memberCount the member count
     */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    /**
     * Gets the unique name for this group.
     *
     * @return the unique name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the unique name for this group.
     *
     * @param systemName the unique name
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Gets the human-readable name for this group.
     *
     * @return the human-readable name
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the human-readable name for this group.
     *
     * @param title the human-readable name
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
