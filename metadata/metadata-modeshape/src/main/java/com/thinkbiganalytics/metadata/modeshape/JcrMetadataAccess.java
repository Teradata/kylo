/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;

/**
 *
 * @author Sean Felten
 */
public class JcrMetadataAccess implements MetadataAccess {
    
    public static final String TBA_PREFIX = "tba";
    
    private static final ThreadLocal<Session> activeSession = new ThreadLocal<Session>() {
        protected Session initialValue() {
            return null;
        }
    };
    
    @Inject
    @Named("metadataJcrRepository")
    private Repository repository;


    public static Session getActiveSession() {
        return activeSession.get();
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#commit(com.thinkbiganalytics.metadata.api.Command)
     */
    @Override
    public <R> R commit(Command<R> cmd) {
        Session session = activeSession.get();
        
        if (session == null) {
            try {
                activeSession.set(this.repository.login());
                try {
                    R result = cmd.execute();
                    activeSession.get().save();
                    return result;
                } finally {
                    activeSession.get().logout();
                    activeSession.remove();
                }
            } catch (RepositoryException e) {
                // TODO Use a better exception
                throw new RuntimeException(e);
            }
        } else {
            return cmd.execute();
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.MetadataAccess#read(com.thinkbiganalytics.metadata.api.Command)
     */
    @Override
    public <R> R read(Command<R> cmd) {
        // TODO Handle rollback/read-only support
        return commit(cmd);
    }
}
