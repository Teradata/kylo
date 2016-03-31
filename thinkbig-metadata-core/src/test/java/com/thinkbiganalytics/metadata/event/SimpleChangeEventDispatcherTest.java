package com.thinkbiganalytics.metadata.event;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseDirectoryDataset;
import com.thinkbiganalytics.metadata.core.dataset.files.BaseFileList;
import com.thinkbiganalytics.metadata.core.op.BaseChangeSet;

public class SimpleChangeEventDispatcherTest {
    
    private SimpleChangeEventDispatcher dispatcher = new SimpleChangeEventDispatcher();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws Exception {
        final AtomicBoolean bool = new AtomicBoolean(false);
        
        this.dispatcher.addListener(new DataChangeEventListener<DirectoryDataset, FileList>() {
            @Override
            public void handleEvent(DataChangeEvent<DirectoryDataset, FileList> event) {
                bool.set(true);
            }
        });
        
        BaseDirectoryDataset dataset = new BaseDirectoryDataset("test", "test", Paths.get("/tmp"));
        BaseFileList content = new BaseFileList(Arrays.<Path>asList(Paths.get("file1.txt"), Paths.get("file2.txt")));
        BaseChangeSet<DirectoryDataset, FileList> changeSet = new BaseChangeSet<DirectoryDataset, FileList>(dataset, content);
        
        this.dispatcher.nofifyChange(changeSet);
        
        assertTrue(bool.get());
    }

}
