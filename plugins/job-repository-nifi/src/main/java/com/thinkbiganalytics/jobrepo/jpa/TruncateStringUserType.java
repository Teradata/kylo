package com.thinkbiganalytics.jobrepo.jpa;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * annotate your String column with something like this
 *
 * @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType",parameters = {@Parameter(name = "length",value = "250")})
 *
 * Created by sr186054 on 8/31/16.
 */
public class TruncateStringUserType implements UserType, ParameterizedType {

    public static final String NAME = "truncateString";

    private Integer maxLength = null;

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return (x == y) || (x != null && y != null && (x.equals(y)));
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        assert (o != null);
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object o) throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(resultSet, names[0], sessionImplementor, o);
        return val == null ? null : val.trim();
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
        String val = (String) o;
        if (val != null) {
            if (maxLength != null && maxLength > 0 && val.length() > maxLength) {
                val = val.substring(0, maxLength);
            }
        }
        preparedStatement.setString(i, val);
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        if (o == null) {
            return null;
        }
        return new String(((String) o));
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object o) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            final String lengthString = parameters.getProperty("length");
            try {
                if (StringUtils.isNotBlank(lengthString)) {
                    this.maxLength = Integer.parseInt(lengthString);
                }
            } catch (final NumberFormatException e) {

            }
        }
    }
}
