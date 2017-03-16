package com.thinkbiganalytics.feedmgr.service;

import org.springframework.cloud.config.server.encryption.EncryptionController;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;

import javax.inject.Inject;

/**
 * Created by sr186054 on 3/15/17.
 */
public class EncryptionService {

    @Inject
    TextEncryptorLocator encryptor;

    @Inject
    EncryptionController encryptionController;


    public static String encrypt(String str){
return str;
    }

    public static String decrypt(String str){
return str;
    }

}
