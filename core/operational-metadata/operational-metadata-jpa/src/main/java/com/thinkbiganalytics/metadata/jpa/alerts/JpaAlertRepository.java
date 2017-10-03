/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.alerts;

/*-
 * #%L
 * thinkbig-alerts-default
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

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 *
 */
public interface JpaAlertRepository extends JpaRepository<JpaAlert, JpaAlert.ID>, JpaSpecificationExecutor<JpaAlert> {


    @Query("select alert from JpaAlert as alert "
           + "where alert.createdTime > :time")
    List<JpaAlert> findAlertsAfter(@Param("time") DateTime time);


    @Query("select distinct alert.typeString from JpaAlert as alert")
    Set<String> findAlertTypes();

    @Query("select max(alert.modifiedTimeMillis) from JpaAlert as alert")
    Long findMaxUpdatedTime();


}
