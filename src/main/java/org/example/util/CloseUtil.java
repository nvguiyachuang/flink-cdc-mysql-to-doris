package org.example.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {

    public static void close(AutoCloseable... connArr) {
        if (null != connArr && connArr.length > 0) {
            for (AutoCloseable closeable : connArr) {
                if (null != closeable) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        // throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
