package com.thinkbiganalytics.metadata.modeshape.common;

import javax.jcr.Node;

/**
 * Created by sr186054 on 6/6/16.
 */
public class AbstractJcrSystemEntity extends JcrPropertiesEntity {

    public AbstractJcrSystemEntity(Node node) {
        super(node);
    }

    public static final String TITLE = "jcr:title";
    public static final String SYSTEM_NAME = "tba:systemName";
    public static final String DESCRIPTION = "jcr:description";


    public void setSystemName(String systemName){
        setProperty(SYSTEM_NAME,systemName);
    }

    public void setDescription(String description){
        setProperty(DESCRIPTION,description);
    }

    public String getDescription() {
        return getProperty(DESCRIPTION,String.class);
    }

    public String getSystemName() {
        return getProperty(SYSTEM_NAME,String.class);
    }

    public String getTitle(){
        return  getProperty(TITLE, String.class);
    }
    public void setTitle(String title){
        setProperty(TITLE,title);
    }


}
