package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Queue to drain and send to ops Manager
 */
public class JmsSenderConsumer implements Runnable {

    private BlockingQueue<JmsSender> queue;


    public JmsSenderConsumer(BlockingQueue<JmsSender> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            while (queue.peek() != null) {

                List<JmsSender> items = new ArrayList<>();
                queue.drainTo(items);
                if (!items.isEmpty()) {
                    items.stream().forEach(item -> item.run());
                }
            }
        }
    }
}
