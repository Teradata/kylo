package com.thinkbiganalytics.metadata.modeshape.user;

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link User} objects stored in a JCR repository.
 */
public class JcrUserProvider extends BaseJcrProvider<User, User.ID> implements UserProvider {

    @Nonnull
    @Override
    public User ensureUser(@Nonnull final String systemName) {
        final Map<String, Object> props = new HashMap<>();
        props.put(JcrUser.SYSTEM_NAME, systemName);
        return findOrCreateEntity("/metadata/security/users", systemName, props);
    }

    @Nonnull
    @Override
    public Optional<User> findBySystemName(@Nonnull final String systemName) {
        final String query = "SELECT * FROM [" + getNodeType(getJcrEntityClass()) + "] AS user WHERE user.[" + JcrUser.SYSTEM_NAME + "] = '" + StringEscapeUtils.escapeSql(systemName) + "'";
        return Optional.ofNullable(findFirst(query));
    }

    @Override
    public Class<? extends User> getEntityClass() {
        return JcrUser.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrUser.class;
    }

    @Override
    public String getNodeType(@Nonnull final Class<? extends JcrEntity> jcrEntityType) {
        return JcrUser.NODE_TYPE;
    }

    @Override
    public User.ID resolveId(@Nonnull final Serializable fid) {
        return new JcrUser.UserId(fid);
    }
}
