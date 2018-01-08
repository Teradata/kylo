package com.thinkbiganalytics.feedmgr.util;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import org.hashids.Hashids;

/**
 * Routine to identify a unique short string id.
 */
public class UniqueIdentifier {

    /**
     * this is not used for anything security related.  no need to hide the salt.
     */
    private static String salt = "unique identifier";
    private static int idLength = 8;
    private static Hashids hashids = new Hashids(salt,idLength);

    public UniqueIdentifier(){

    }

    public static String encode(Long from){
        return hashids.encode(from);
    }

    public static long[] decode(String id){
        return hashids.decode(id);
    }


}
