package com.thinkbiganalytics.datalake.authorization.model;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper; 
/**
 * 
 *  Created by Shashi Vishwakarma on 19/9/2016.
 *
 */
public class SentrySearchPolicyMapper  implements RowMapper<SentrySearchPolicy>{

    private final String ROLE = "role";
    @Override
    public SentrySearchPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {

        SentrySearchPolicy searchPolicy = new SentrySearchPolicy();
        searchPolicy.setRole(rs.getString(ROLE));
        return searchPolicy;
    }
}
