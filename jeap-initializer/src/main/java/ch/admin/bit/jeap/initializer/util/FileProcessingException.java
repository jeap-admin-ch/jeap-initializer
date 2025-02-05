package ch.admin.bit.jeap.initializer.util;

import java.io.IOException;

public class FileProcessingException extends RuntimeException {
    private FileProcessingException(String message, Exception cause) {
        super(message, cause);
    }

    public static FileProcessingException ioException(IOException cause) {
        return new FileProcessingException("I/O Exception", cause);
    }

    public static FileProcessingException ioException(String msg, IOException cause) {
        return new FileProcessingException(msg, cause);
    }
}
