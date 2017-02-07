package com.thinkbiganalytics.datalake.authorization.model;

/*-
 * #%L
 * thinkbig-sentry-client
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
public class SentrySearchPolicyMapper implements RowMapper<SentrySearchPolicy> {

    private static final String ROLE = "role";

    @Override
    public SentrySearchPolicy mapRow(ResultSet rs, int rowNum) throws SQLException {

        SentrySearchPolicy searchPolicy = new SentrySearchPolicy();
        searchPolicy.setRole(rs.getString(ROLE));
        return searchPolicy;
    }
}
