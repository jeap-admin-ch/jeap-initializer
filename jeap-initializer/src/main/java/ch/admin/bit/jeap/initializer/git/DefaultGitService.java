package ch.admin.bit.jeap.initializer.git;

import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class DefaultGitService implements GitService {

    @Override
    public void cloneRepositoryAtPath(GitRepositoryConfiguration configuration, Path localPath) {
        log.info("Cloning repository {} (Git ref: {}) to {}", configuration.getUrl(), configuration.getReference(), localPath);

        CloneCommand cloneCommand = createCloneCommand(configuration, localPath);

        try (Git ignored = cloneCommand.call()) {
            FileSystemUtils.deleteRecursively(Path.of(localPath + File.separator + ".git"));
        } catch (GitAPIException | IOException e) {
            throw GitException.cloneFailed(e);
        }
    }

    @Override
    public String getFileContentFromRepository(GitRepositoryConfiguration configuration, String filePath) throws IOException {
        log.info("Getting file {} from repository {}", filePath, configuration.getUrl());
        Path localPath = Files.createTempDirectory("jeap-initializer");
        try {
            CloneCommand cloneCommand = createCloneCommand(configuration, localPath);

            try (Git result = cloneCommand.call()) {
                Repository repository = result.getRepository();
                ObjectId headId = repository.resolve("HEAD");
                if (headId == null) {
                    throw GitException.fileRetrievalFailed("HEAD could not be resolved");
                }

                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(headId);
                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(commit.getTree());
                        treeWalk.setRecursive(true);
                        treeWalk.setFilter(org.eclipse.jgit.treewalk.filter.PathFilter.create(filePath));
                        if (!treeWalk.next()) {
                            throw GitException.fileRetrievalFailed("File %s not found in repository %s".formatted(filePath, configuration.getUrl()));
                        }
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        return new String(loader.getBytes(), UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            throw GitException.cloneFailed(e);
        } finally {
            FileSystemUtils.deleteRecursively(localPath);
        }
    }

    private static CloneCommand createCloneCommand(GitRepositoryConfiguration configuration, Path localPath) {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(configuration.getUrl())
                .setDepth(1) // shallow clone to speed up clone - history is not required
                .setDirectory(localPath.toFile());
        cloneCommand.setBranch(configuration.getReference());
        if (hasText(configuration.getPassword())) {
            cloneCommand = cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(configuration.getUser(), configuration.getPassword()));
        }
        return cloneCommand;
    }
}
