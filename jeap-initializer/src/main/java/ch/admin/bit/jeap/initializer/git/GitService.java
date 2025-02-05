package ch.admin.bit.jeap.initializer.git;

import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;

import java.io.IOException;
import java.nio.file.Path;

public interface GitService {

    void cloneRepositoryAtPath(GitRepositoryConfiguration configuration, Path localPath);

    String getFileContentFromRepository(GitRepositoryConfiguration configuration, String filePath) throws IOException;
}
