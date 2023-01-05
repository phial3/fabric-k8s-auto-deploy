package org.bc.auto.utils;

import java.util.Locale;
import java.util.UUID;

public class StringUtils {

    public synchronized static String getId() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.toLowerCase(Locale.ROOT);
        uuid = uuid.replaceAll("-", "");

        return uuid;
    }

}
