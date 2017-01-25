/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import java.io.Serializable;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

/**
 *
 * @author Sean Felten
 */
public interface HighWaterMarkState {

    <W extends Serializable> W loadWaterMark(ProcessSession session, FlowFile ff, String key) throws WaterMarkActiveException;
    
    <W extends Serializable> FlowFile recordWaterMark(ProcessSession session, 
                                                      FlowFile ff, 
                                                      String key, 
                                                      W markValue);
    
    FlowFile commitWaterMark(ProcessSession session, FlowFile ff, String key);
    
    FlowFile commitAllWaterMarks(ProcessSession session, FlowFile ff);
    
    FlowFile releaseWaterMark(ProcessSession session, FlowFile ff, String key);
    
    FlowFile releaseAllWaterMarks(ProcessSession session, FlowFile ff);
    
}
