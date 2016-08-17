package com.thinkbiganalytics.metadata.modeshape.user;

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link User} objects stored in a JCR repository.
 */
public class JcrUserProvider extends BaseJcrProvider<User, User.ID> implements UserProvider {

    @Nonnull
    @Override
    public Optional<User> findByUsername(@Nonnull final String username) {
        final String query = "SELECT * FROM [" + getNodeType(getJcrEntityClass()) + "] AS user WHERE user.[" + JcrUser.SYSTEM_NAME + "] = '" + StringEscapeUtils.escapeSql(username) + "'";
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
