package it.pagopa.pn.workflowmanager.action.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

    @Test
    void getKeyWithStoragePrefixAddsPrefixWhenMissing() {
        assertEquals("safestorage://file-key", FileUtils.getKeyWithStoragePrefix("file-key"));
    }

    @Test
    void getKeyWithStoragePrefixReturnsSameKeyWhenPrefixAlreadyExists() {
        assertEquals("safestorage://file-key", FileUtils.getKeyWithStoragePrefix("safestorage://file-key"));
    }
}

