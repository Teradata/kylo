package com.thinkbiganalytics.service;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
public class DefaultServicesApplicationStartup implements ServicesApplicationStartup, ApplicationListener<ContextRefreshedEvent> {

    private DateTime startTime = null;
    private List<ServicesApplicationStartupListener> startupListeners = new ArrayList<>();

    int maxThreads = 10;

    ExecutorService executorService =
        new ThreadPoolExecutor(
            maxThreads, // core thread pool size
            maxThreads, // maximum thread pool size
            10, // time to wait before resizing pool
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxThreads, true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public void subscribe(ServicesApplicationStartupListener o) {
        startupListeners.add(o);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (startTime == null) {
            startTime = new DateTime();
            for (ServicesApplicationStartupListener startupListener : startupListeners) {
                executorService.submit(new StartupTask(startTime, startupListener));
            }
        }
    }

    private class StartupTask implements Runnable {

        ServicesApplicationStartupListener listener;
        DateTime startTime;

        public StartupTask(DateTime startTime, ServicesApplicationStartupListener listener) {
            this.startTime = startTime;
            this.listener = listener;
        }

        public void run() {
            listener.onStartup(startTime);
        }
    }
}
