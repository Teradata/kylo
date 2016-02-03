package com.thinkbiganalytics.metadata.event;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.omg.CosNaming.IstringHelper;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.event.ChangeEvent;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
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
        
        this.dispatcher.addListener(new ChangeEventListener<DirectoryDataset, FileList>() {
            @Override
            public void handleEvent(ChangeEvent<DirectoryDataset, FileList> event) {
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
