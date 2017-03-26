package com.thinkbiganalytics.feedmgr.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class UploadProgressService {


    private final Cache<String,UploadProgress>uploadProgress = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();


    public String newUpload(){
        String key = UUID.randomUUID().toString();
        return newUpload(key);
    }

    public String newUpload(String key){
        uploadProgress.put(key,new UploadProgress(key));
        return key;
    }

    public void removeUpload(String key){
        //uploadProgress.invalidate(key);
    }


    public UploadProgressMessage addUploadStatus(String uploadKey, String message) {
        return addUploadStatus(uploadKey,message,false,false);
    }

    public UploadProgressMessage addUploadStatus(String uploadKey, String message, boolean complete, boolean success) {
        UploadProgress uploadProgress = getUploadStatus(uploadKey);
       if(uploadProgress != null){
           UploadProgressMessage uploadProgressMessage = new UploadProgressMessage(message);
          if(complete) {
             uploadProgressMessage.complete(success);
          }
           uploadProgress.getMessages().add(uploadProgressMessage);

           return uploadProgressMessage;
       }
       return null;
    }

    public UploadProgressMessage updateUploadStatus(String uploadKey, String messageKey,String message, boolean complete) {
        UploadProgress uploadProgress = getUploadStatus(uploadKey);
        if(uploadProgress != null){
            UploadProgressMessage uploadProgressMessage = uploadProgress.getMessage(messageKey);
            if(uploadProgressMessage == null){
                uploadProgressMessage = new UploadProgressMessage();
                uploadProgressMessage.setMessageKey(messageKey);
            }
            uploadProgressMessage.setMessage(message);
            uploadProgressMessage.setDateTime(DateTime.now());
            uploadProgressMessage.setComplete(complete);
            uploadProgress.getMessages().add(new UploadProgressMessage(message));
            return uploadProgressMessage;
        }
        return null;
    }

    public UploadProgress getUploadStatus(String key){
        return uploadProgress.getIfPresent(key);
    }

    public void removeMessage(String uploadKey, UploadProgressMessage message){
        UploadProgress progress = getUploadStatus(uploadKey);
        if(progress != null) {
            progress.getMessages().remove(message);
        }
    }

}
