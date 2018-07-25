/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * Identifies a system-level entity that has a "system name" that is distinct from its title and description.
 */
public interface SystemEntity {

    /**
     * @return the system name of the entity (usually immutable)
     */
    String getSystemName();
    
    /**
     * @param name the new system name
     */
    void setSystemName(String name);

    /**
     * @return the title
     */
    String getTitle();

    /**
     * @param title the new title
     */
    void setTitle(String title);
    
    /**
     * @return the description
     */
    String getDescription();
    
    /**
     * @param description the new description
     */
    void setDescription(String description);

}
