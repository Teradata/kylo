package com.thinkbiganalytics.nifi.provenance.model.util;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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

import java.util.concurrent.atomic.AtomicLong;


public class LongIdGenerator {

    final static AtomicLong kyloEventIdGenerator = new AtomicLong(0);


    public static Long nextId(){
       kyloEventIdGenerator.compareAndSet(Long.MIN_VALUE,0);
       return kyloEventIdGenerator.decrementAndGet();
    }
}
