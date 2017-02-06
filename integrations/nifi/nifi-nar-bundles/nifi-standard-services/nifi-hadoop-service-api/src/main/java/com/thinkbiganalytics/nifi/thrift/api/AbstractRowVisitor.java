package com.thinkbiganalytics.nifi.thrift.api;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service-api
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

import java.sql.ResultSet;
import java.util.Date;

public class AbstractRowVisitor implements RowVisitor {

    @Override
    public void visitRow(ResultSet row) {
    }

    @Override
    public void visitColumn(String columnName, int colType, Date value) {
    }

    @Override
    public void visitColumn(String columnName, int colType, String value) {
    }
}
