package com.thinkbiganalytics.metadata.modeshape.support;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

/**
 * Created by sr186054 on 6/13/16.
 */
public class JcrVersionUtil {

    /**
     * Returns the LockManager object from the given session.
     *
     * @param session {@link Session}
     * @return a {@link LockManager} object
     */
    public static LockManager getLockManager(Session session) throws RepositoryException {
        LockManager lockMgr = session.getWorkspace().getLockManager();
        return lockMgr;
    }

    /**
     * Returns the VersionManager object from the given session.
     *
     * @param session {@link Session}
     * @return a {@link VersionManager} object
     */
    public static VersionManager getVersionManager(Session session) throws RepositoryException {
        VersionManager versionMgr = session.getWorkspace().getVersionManager();
        return versionMgr;
    }

    /**
     * Sets the given node to checked-out status.
     *
     * @param node node to check-out
     */
    public static void checkout(Node node) throws RepositoryException {
        getVersionManager(node.getSession()).checkout(node.getPath());
    }


    /**
     * Creates for the given node a new version and returns that version. Put the node into the checked-in state.
     *
     * @param node node to checkin
     * @return the created version
     */
    public static Version checkin(Node node) throws RepositoryException {
        return getVersionManager(node.getSession()).checkin(node.getPath());
    }

    public static void checkinRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkinRecursively(it.nextNode());
            }
            if (node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkin();
                checkin(node);
            }

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform check-in", e);
        }
    }

    public static void checkoutRecursively(Node node) {
        try {
            NodeIterator it = node.getNodes();
            while (it.hasNext()) {
                checkoutRecursively(it.nextNode());
            }
            if (!node.isCheckedOut() && node.isNodeType(NodeType.MIX_VERSIONABLE)) {
                //node.checkout();
                checkout(node);
            }

        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform check-out", e);
        }
    }


    /**
     * Places a lock on the given node.
     *
     * @param node            the node to be locked
     * @param isDeep          if <code>true</code> this lock will apply to this node and all its descendants; if <code>false</code>, it applies only to this node.
     * @param isSessionScoped if <code>true</code>, this lock expires with the current session; if <code>false</code> it expires when explicitly or automatically unlocked for some other reason.
     * @param timeoutHint     desired lock timeout in seconds (servers are free to ignore this value); specify Long.MAX_VALUE for no timeout.
     * @param ownerInfo       a string containing owner information supplied by the client; servers are free to ignore this value.
     * @see javax.jcr.lock.LockManager#lock(String, boolean, boolean, long, String)
     */
    public static void lock(Node node, boolean isDeep, boolean isSessionScoped, long timeoutHint, String ownerInfo) {
        try {
            getLockManager(node.getSession()).lock(node.getPath(), isDeep, isSessionScoped, timeoutHint, ownerInfo);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform lock", e);
        }
    }

    /**
     * Removes the lock on the given node.
     *
     * @param node the node unlock
     * @see javax.jcr.lock.LockManager#unlock(String)
     */
    public static void unlock(Node node) {
        try {
            getLockManager(node.getSession()).unlock(node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Could not perform unlock", e);
        }
    }

    public static Version getBaseVersion(Node node) {
        String nodeName = null;
        try {
            nodeName = node.getName();
            return JcrVersionUtil.getVersionManager(node.getSession()).getBaseVersion(node.getPath());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Base Version for " + nodeName, e);
        }
    }

    public static List<Version> getVersions(Node node) {
        String nodeName = null;
        try {
            nodeName = node.getName();
            List<Version> versions = new ArrayList<>();
            VersionHistory history = JcrVersionUtil.getVersionManager(node.getSession()).getVersionHistory(node.getPath());
            VersionIterator itr = history.getAllVersions();

            while (itr.hasNext()) {
                Version version = itr.nextVersion();
                versions.add(version);
            }
            return versions;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Version History for " + nodeName, e);
        }

    }

    public static <T extends JcrObject> T getVersionedNode(Version version, Class<T> type, Object[] constructorArgs) {
        String nodeName = null;
        String versionName = null;
        try {
            versionName = version.getName();
            Node node = version.getFrozenNode();
            nodeName = node.getName();
            return JcrUtil.constructNodeObject(node, type, constructorArgs);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find Version " + versionName + " for Node " + nodeName, e);
        }
    }

    public static Version findVersion(Node node, final String versionName) {
        Version version = Iterables.tryFind(getVersions(node), new Predicate<Version>() {
            @Override
            public boolean apply(Version version) {
                try {
                    return version.getName().equalsIgnoreCase(versionName);
                } catch (RepositoryException e) {

                }
                return false;
            }
        }).orNull();
        return version;
    }


    public static <T extends JcrObject> T getVersionedNode(Version version, Class<T> type) {
        return getVersionedNode(version, type, null);
    }

    public static <T extends JcrObject> T getVersionedNode(JcrObject node, String versionNumber, Class<T> type) {
        Version version = findVersion(node.getNode(), versionNumber);
        if (version != null) {
            return getVersionedNode(version, type);
        }
        throw new MetadataRepositoryException("Unable to find Version " + versionNumber + " for Node " + node.getNodeName());
    }




}
