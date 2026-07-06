package it.pagopa.pn.workflowmanager.action.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    public static final String SAFE_STORAGE_URL_PREFIX = "safestorage://";

    @NotNull
    public static String getKeyWithStoragePrefix(String key) {
        if(key.startsWith(SAFE_STORAGE_URL_PREFIX)){
            return key;
        }
        return SAFE_STORAGE_URL_PREFIX + key;
    }
}
