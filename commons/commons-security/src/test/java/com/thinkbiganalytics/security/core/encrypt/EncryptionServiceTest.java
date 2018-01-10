package com.thinkbiganalytics.security.core.encrypt;

import com.thinkbiganalytics.security.core.SecurityCoreConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityCoreConfig.class, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@TestPropertySource(properties = {
    "encrypt.key=sEIpFztjghM3Ns1HabcQltcBtC7Uiqanb4vI9YWzVnKgzF3VTrrTFQvtGNwQKD4B/XrFdaHJeLid"})
public class EncryptionServiceTest {

    @Inject
    private EncryptionService encryptionService;


    @Test
    public void testIsNotEncrypted() {
        char[] testValue = {'t', 'h', 'i', 's', 'i', 's', 'a', 't', 'e', 's', 't'};
        assertFalse("The value is expected to not be encrypted ", encryptionService.isEncrypted(String.valueOf(testValue)));
    }

    @Test
    public void testIsEncrypted() {
        char[] testValue = {'{', 'c', 'i', 'p', 'h', 'e', 'r', '}', 't', 'h', 'i', 's', 'i', 's', 'e', 'n', 'c', 'r', 'p', 't', 'e', 'd'};
        assertTrue("The value is expected to not be encrypted ", encryptionService.isEncrypted(String.valueOf(testValue)));
    }

    @Test
    public void testIsEncryptedShortValue() {
        char[] testValue = {'h', 'i'};
        assertFalse("The value is expected to not be encrypted ", encryptionService.isEncrypted(String.valueOf(testValue)));
    }

    @Test
    public void testIsEncryptedArrayOutOfBounds() {
        char[] testValue = {'{', 'c'};
        assertFalse("The value is expected to not be encrypted ", encryptionService.isEncrypted(String.valueOf(testValue)));
    }


}
