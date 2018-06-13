package com.thinkbiganalytics.spark.rest.filemetadata.tasks;

/*-
 * #%L
 * kylo-spark-shell-controller
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.spark.rest.controller.SparkShellProxyController;
import com.thinkbiganalytics.spark.rest.controller.SparkShellScriptRunner;
import com.thinkbiganalytics.spark.rest.controller.SparkShellUserProcessService;
import com.thinkbiganalytics.spark.rest.filemetadata.FileMetadataSchemaScriptBuilder;
import com.thinkbiganalytics.spark.rest.filemetadata.FileMetadataTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.model.FileMetadataResponse;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Service that organizes tasks to find header metadata using spark.
 * It allows for a calling task to wait until all tasks are done before completing
 */
@Component
public class FileMetadataTaskService {

    private static final Logger log = LoggerFactory.getLogger(FileMetadataTaskService.class);

    public static String APPLICATION_AVRO = "application/avro";
    public static String APPLICATION_PARQUET = "application/parquet";
    public static String APPLICATION_XML = "application/xml";
    public static String APPLICATION_ORC = "application/orc";
    public static String APPLICATION_JSON = "application/json";

    public static List<String> PARSABLE_MIME_TYPES = Lists.newArrayList(APPLICATION_AVRO, APPLICATION_ORC, APPLICATION_PARQUET, APPLICATION_XML, APPLICATION_JSON);


    @Inject
    private SparkShellRestClient restClient;

    @Inject
    private SparkShellUserProcessService sparkShellUserProcessService;

    private ExecutorService executor = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("kylo-filemetadata-pool-%d").build());


    private Cache<String, FileMetadataCompletionTask> metadataResultCache;

    public FileMetadataTaskService() {
        metadataResultCache = createCache();
    }


    public void setSparkShellUserProcessService(SparkShellUserProcessService sparkShellUserProcessService) {
        this.sparkShellUserProcessService = sparkShellUserProcessService;
    }

    public void setRestClient(SparkShellRestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Creates a cache of active file metadata jobs
     */
    @Nonnull
    private Cache<String, FileMetadataCompletionTask> createCache() {
        final Cache<String, FileMetadataCompletionTask> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

        return cache;
    }


    public String getUsername() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String username = (auth.getPrincipal() instanceof User) ? ((User) auth.getPrincipal()).getUsername() : auth.getPrincipal().toString();
        return username;
    }

    public void removeFromCache(String tableId) {
        metadataResultCache.invalidate(tableId);
    }

    public FileMetadataCompletionTask get(String tableId) {
        return metadataResultCache.getIfPresent(tableId);
    }

    /**
     * Group the files by their respective mime type
     * For each mime type that spark can process create a task to determine the header information
     */
    public void findFileMetadataSchemas(ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse, FileMetadataTransformResponseModifier resultModifier) {
        FileMetadataCompletionTask result = new FileMetadataCompletionTask(modifiedTransformResponse, resultModifier);
        metadataResultCache.put(result.getTableId(), result);

        Map<String, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata>> mimeTypeGroup = resultModifier.groupByMimeType();

        List<String> mimeTypes = Lists.newArrayList(mimeTypeGroup.keySet());
        mimeTypes.removeIf(type -> !PARSABLE_MIME_TYPES.contains(type));

        List<SparkShellScriptRunner> tasks = new ArrayList<>();
        for (String mimeType : mimeTypes) {
            List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata> data = mimeTypeGroup.get(mimeType);
            if (mimeType == "application/xml") {
                //need to group by rowtag
                Map<String, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata>> rowTags = data.stream().collect(Collectors.groupingBy(row -> row.getRowTag()));
                for (Map.Entry<String, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata>> rows : rowTags.entrySet()) {
                    List<String> files = rows.getValue().stream().map(r -> r.getFilePath()).collect(Collectors.toList());
                    SparkShellScriptRunner
                        shellScriptRunner =
                        new SparkShellScriptRunner(sparkShellUserProcessService, restClient, getUsername(), FileMetadataSchemaScriptBuilder.getSparkScript(mimeType, rows.getKey(), files), mimeType);
                    tasks.add(shellScriptRunner);
                    result.addTask(shellScriptRunner, rows.getValue());
                }

            } else {
                List<String> files = data.stream().map(r -> r.getFilePath()).collect(Collectors.toList());
                SparkShellScriptRunner
                    shellScriptRunner =
                    new SparkShellScriptRunner(sparkShellUserProcessService, restClient, getUsername(), FileMetadataSchemaScriptBuilder.getSparkScript(mimeType, null, files), mimeType);
                tasks.add(shellScriptRunner);
                result.addTask(shellScriptRunner, data);
            }


        }
        submitTasks(result, tasks);
    }

    private void submitTasks(FileMetadataCompletionTask result, List<SparkShellScriptRunner> tasks) {
        int parties = tasks.size();
        if(parties == 0) {
            result.run();
        }
        else {
            int threadPoolSize = ((ThreadPoolExecutor) executor).getCorePoolSize();
            int activeThreads = ((ThreadPoolExecutor) executor).getActiveCount();
            //dont add to the pool unless we have at least the correct slots open
            if (threadPoolSize - activeThreads < parties) {
                waitingTasksQueue.add(new QueuedTasks(result, tasks));
            } else {
                CyclicBarrier barrier = new CyclicBarrier(parties, new FinalMetadataSchemaTaskWrapper(result, tasks));
                tasks.stream().forEach(t -> {
                    Future f = executor.submit(new FileMetadataSchemaTask(barrier, t));
                });
          /*
            try {
                barrier.await();
            } catch (Exception e) {
                log.error("Error clearing final barrier for file metadata schema tasks {} ",e.getMessage(),e);
            }
            */
            }
        }
    }

    private void checkQueue() {
        QueuedTasks next = waitingTasksQueue.peek();
        if (next != null) {
            int threadPoolSize = ((ThreadPoolExecutor) executor).getCorePoolSize();
            int activeThreads = ((ThreadPoolExecutor) executor).getActiveCount();
            //dont add to the pool unless we have at least the correct slots open
            if (threadPoolSize - activeThreads >= next.getTasks().size()) {
                try {
                    next = waitingTasksQueue.take();
                    //we can process
                    //add to executor
                    submitTasks(next.getFileMetadataResult(), next.getTasks());
                } catch (Exception e) {
                    log.error("Error submitting file metadata schema tasks {} ",e.getMessage(),e);
                }

            }
        }
    }

    private class QueuedTasks {

        FileMetadataCompletionTask fileMetadataResult;
        List<SparkShellScriptRunner> tasks;

        public QueuedTasks(FileMetadataCompletionTask fileMetadataResult, List<SparkShellScriptRunner> tasks) {
            this.fileMetadataResult = fileMetadataResult;
            this.tasks = tasks;
        }

        public FileMetadataCompletionTask getFileMetadataResult() {
            return fileMetadataResult;
        }

        public List<SparkShellScriptRunner> getTasks() {
            return tasks;
        }
    }

    BlockingQueue<QueuedTasks> waitingTasksQueue = new LinkedBlockingQueue<QueuedTasks>();

    /**
     * Threaded Task that releases the barrier counter
     */
    private class FileMetadataSchemaTask implements Runnable {

        CyclicBarrier barrier;
        Runnable runnable;
        String name;

        public FileMetadataSchemaTask(CyclicBarrier barrier, Runnable runnable) {
            this.barrier = barrier;
            this.runnable = runnable;
            this.name = UUID.randomUUID().toString();
        }

        public void run() {
            try {
                this.runnable.run();
            } catch (Exception e) {
                log.error("Error running file metadata schema task {} ",e.getMessage(),e);
            } finally {
                try {
                    barrier.await();
                } catch (Exception e) {
            log.error("Error clearing file metadata schema barrier {} ",e.getMessage(),e);
                }
            }
        }

    }

    private class FinalMetadataSchemaTaskWrapper implements Runnable {
        private FileMetadataCompletionTask finalTask;
        private List<SparkShellScriptRunner> tasks;

        public FinalMetadataSchemaTaskWrapper(FileMetadataCompletionTask task, List<SparkShellScriptRunner>tasks){
            this.finalTask = task;
            this.tasks = tasks;
        }

        @Override
        public void run() {
            List<String> exceptions = new ArrayList<>();
            try {
                try {
                    this.finalTask.run();
                } catch (Exception e) {
                    log.error("Error running file metadata schema task {} ", e.getMessage(), e);
                    exceptions.add(e.getMessage());
                }

            }
            finally {
                checkQueue();
            }


        }
    }


}
