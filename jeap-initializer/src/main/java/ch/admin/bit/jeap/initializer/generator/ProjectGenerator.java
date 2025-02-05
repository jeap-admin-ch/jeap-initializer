package ch.admin.bit.jeap.initializer.generator;

import ch.admin.bit.jeap.initializer.config.TemplateModuleNotFoundException;
import ch.admin.bit.jeap.initializer.config.TemplateParameterMissingException;
import ch.admin.bit.jeap.initializer.contributor.ProjectContributor;
import ch.admin.bit.jeap.initializer.git.GitException;
import ch.admin.bit.jeap.initializer.git.GitService;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.aop.MeterTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class ProjectGenerator {

    public static final String GITOPS_DIRECTORY = "gitops";

    private final TemplateService templateService;
    private final GitService gitService;
    private final List<ProjectContributor> projectContributorList;

    @Counted(value = "generateProject", description = "Counts project generation requests")
    public void generate(@MeterTag(key = "template", value = "#projectRequest.template") ProjectRequest projectRequest, Path localPath) throws IOException {
        long startedAt = System.currentTimeMillis();
        ProjectTemplate projectTemplate = templateService.getTemplate(projectRequest.getTemplate());
        log.info("Generating project from template {} due to request {}", projectTemplate, projectRequest);

        assertTemplateParametersAreProvided(projectRequest, projectTemplate);
        assertModuleSelectionIsValid(projectRequest, projectTemplate);
        assertModuleParametersAreProvided(projectRequest, projectTemplate);

        gitService.cloneRepositoryAtPath(projectTemplate.getRepositoryConfiguration(), localPath);

        if (projectTemplate.getGitOpsRepositoryConfiguration() != null) {
            Path gitOpsPath = Path.of(localPath + File.separator + GITOPS_DIRECTORY);
            Files.createDirectories(gitOpsPath);
            gitService.cloneRepositoryAtPath(projectTemplate.getGitOpsRepositoryConfiguration(), gitOpsPath);
        }

        log.info("Starting project generation at {}", localPath);
        for (ProjectContributor projectContributor : projectContributorList) {
            long contributorStartedAt = System.currentTimeMillis();
            projectContributor.contribute(localPath, projectRequest, projectTemplate);
            log.info("Contributor {} done in {}ms", projectContributor.getClass().getSimpleName(), System.currentTimeMillis() - contributorStartedAt);
        }

        initializeGitRepository(localPath);

        log.info("Project generation done in {} ms", System.currentTimeMillis() - startedAt);
    }

    private void initializeGitRepository(Path localPath) {
        log.info("Initializing Git repository at {}", localPath);
        try (Git git = Git.init().setDirectory(localPath.toFile()).call()) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        } catch (GitAPIException e) {
            throw GitException.initFailed(e);
        }
    }

    private static void assertTemplateParametersAreProvided(ProjectRequest projectRequest, ProjectTemplate projectTemplate) {
        projectTemplate.getTemplateParameters().forEach(templateParameter -> {
            String parameterValue = projectRequest.getTemplateParameters().get(templateParameter.getId());
            if (!StringUtils.hasText(parameterValue)) {
                throw new TemplateParameterMissingException(templateParameter.getId());
            }
        });
    }

    private static void assertModuleParametersAreProvided(ProjectRequest projectRequest, ProjectTemplate projectTemplate) {
        projectRequest.getSelectedTemplateModules().forEach(selectedModule -> {
            List<TemplateParameter> requiredParameters = projectTemplate.getTemplateModule(selectedModule.getId()).getModuleParameters();
            requiredParameters.forEach(requiredParameter -> {
                String parameterValue = selectedModule.getModuleParameters().get(requiredParameter.getId());
                if (!StringUtils.hasText(parameterValue)) {
                    throw new TemplateParameterMissingException(requiredParameter.getId());
                }
            });
        });
    }

    private void assertModuleSelectionIsValid(ProjectRequest projectRequest, ProjectTemplate projectTemplate) {
        Set<String> allModuleIds = projectTemplate.getAllModuleIds();
        projectRequest.getSelectedModuleIds().forEach(id -> {
            if (!allModuleIds.contains(id)) {
                throw new TemplateModuleNotFoundException(id, allModuleIds);
            }
        });
    }
}
