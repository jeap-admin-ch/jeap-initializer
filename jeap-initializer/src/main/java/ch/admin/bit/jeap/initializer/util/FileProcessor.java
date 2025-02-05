package ch.admin.bit.jeap.initializer.util;

import java.io.IOException;
import java.nio.file.Path;

public interface FileProcessor {
    void process(Path file) throws IOException;
}
