package ch.admin.bit.jeap.initializer.git;

import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGitServiceTest {

    DefaultGitService defaultGitService = new DefaultGitService();

    @TempDir
    private Path tempDir;

    private static String repoUrl;

    @BeforeAll
    static void setUp() throws Exception {
        File repoDir = new File("target/test-git-service");
        FileUtils.copyDirectory(new File("src/test/resources/test-git-service"), repoDir);
        Git newRepo = Git.init()
                .setDirectory(repoDir)
                .call();
        newRepo.add()
                .addFilepattern("README.md")
                .call();
        newRepo.commit()
                .setMessage("Initial revision")
                .call();

        // Check if the branch already exists
        boolean branchExists = newRepo.branchList()
                .call()
                .stream()
                .anyMatch(ref -> ref.getName().equals("refs/heads/feature/branch-for-tests"));

        if (!branchExists) {
            // Create a new branch and add a commit
            newRepo.branchCreate()
                    .setName("feature/branch-for-tests")
                    .call();
            newRepo.checkout()
                    .setName("feature/branch-for-tests")
                    .call();
            File readmeFile = new File(repoDir, "README.md");
            FileUtils.writeStringToFile(readmeFile, "This file is part of a branch", "UTF-8");
            newRepo.add()
                    .addFilepattern("README.md")
                    .call();
            newRepo.commit()
                    .setMessage("Add README.md for feature branch")
                    .call();

            // Checkout back to master
            newRepo.checkout()
                    .setName("master")
                    .call();
        }

        newRepo.close();
        repoUrl = "file://" + repoDir.getAbsolutePath();
    }

    @Test
    void checkoutRepository() throws IOException {
        GitRepositoryConfiguration gitRepositoryConfiguration = new GitRepositoryConfiguration();
        gitRepositoryConfiguration.setUrl(repoUrl);
        gitRepositoryConfiguration.setReference("master");
        defaultGitService.cloneRepositoryAtPath(gitRepositoryConfiguration, tempDir);
        assertTrue(new File(tempDir + File.separator + "README.md").exists());
        assertFalse(new File(tempDir + File.separator + ".git").exists());
    }

    @Test
    void checkoutRepositoryOtherBranch() throws IOException {
        GitRepositoryConfiguration gitRepositoryConfiguration = new GitRepositoryConfiguration();
        gitRepositoryConfiguration.setUrl(repoUrl);
        gitRepositoryConfiguration.setReference("feature/branch-for-tests");
        defaultGitService.cloneRepositoryAtPath(gitRepositoryConfiguration, tempDir);
        assertTrue(new File(tempDir + File.separator + "README.md").exists());
        assertTrue(Files.readString(Path.of(tempDir + File.separator + "README.md")).contains("This file is part of a branch"));
    }

    @Test
    void getFileContentFromRepository() throws IOException {
        GitRepositoryConfiguration gitRepositoryConfiguration = new GitRepositoryConfiguration();
        gitRepositoryConfiguration.setUrl(repoUrl);
        assertThat(defaultGitService.getFileContentFromRepository(gitRepositoryConfiguration, "README.md"))
                .contains("Getting Started");
    }

    @Test
    void getFileContentFromRepository_onBranch() throws IOException {
        GitRepositoryConfiguration gitRepositoryConfiguration = new GitRepositoryConfiguration();
        gitRepositoryConfiguration.setUrl(repoUrl);
        gitRepositoryConfiguration.setReference("feature/branch-for-tests");
        assertThat(defaultGitService.getFileContentFromRepository(gitRepositoryConfiguration, "README.md"))
                .contains("This file is part of a branch");
    }

}
