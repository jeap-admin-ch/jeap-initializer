package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static org.assertj.core.api.Assertions.assertThat;

class CodeRemoverContributorTest {

    @TempDir
    private Path tempDir;

    @Test
    void blocksInJavaAreRemoved() throws IOException {
        addTestFileToFolder("Jenkinsfile.gitops-deployment", tempDir);
        JeapInitializerProperties props = new JeapInitializerProperties();
        props.setSourceFilesPattern(Pattern.compile(".*"));
        CodeRemoverContributor contributor = new CodeRemoverContributor(props);

        contributor.contribute(tempDir, new ProjectRequest(), new ProjectTemplate());

        String content = Files.readString(tempDir.resolve("Jenkinsfile.gitops-deployment"));
        String expectedContent = """
                @Library('jeap-aws-pipeline@v1') _
                
                gitOpsDeploymentPipeline(
                    mavenImage: 'bit/eclipse-temurin:21',
                    mavenDockerUser: 'jenkins',
                    awsClusterName: 'jme',
                    serviceName: 'bit-jme-app',
                    githubOrganizationName: 'NIVEL-WORKSPACES',
                    githubRepositoryName: 'nivel-jme'
                )
                """;
        assertThat(content)
                .isEqualToIgnoringWhitespace(expectedContent);
    }

    @Test
    void blocksInXmlAreRemoved() throws IOException {
        addTestFileToFolder("pom-module-test.xml", tempDir);
        CodeRemoverContributor contributor = new CodeRemoverContributor("MODULE object-storage", Pattern.compile(".+\\.xml"));

        contributor.contribute(tempDir, new ProjectRequest(), new ProjectTemplate());

        String content = Files.readString(tempDir.resolve("pom-module-test.xml"));
        String expectedContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                
                    <parent>
                        <groupId>ch.admin.bit.jeap</groupId>
                        <artifactId>jeap-spring-boot-parent</artifactId>
                        <version>25.4.0</version>
                        <relativePath/>
                    </parent>
                
                    <groupId>ch.admin.bit.jme</groupId>
                    <artifactId>bit-jme-app</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <packaging>pom</packaging>
                
                    <modules>
                        <module>bit-jme-app-ui</module>
                        <module>bit-jme-app-service</module>
                    </modules>
                
                    <dependencies>
                
                    </dependencies>
                </project>
                """;

        assertThat(content)
                .isEqualToIgnoringWhitespace(expectedContent);
    }
}
