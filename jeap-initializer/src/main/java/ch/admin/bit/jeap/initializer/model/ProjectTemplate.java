package ch.admin.bit.jeap.initializer.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Data
public class ProjectTemplate {

    private String key;
    private String name = "";
    private String description = "";
    private String basePackage;
    private GitRepositoryConfiguration repositoryConfiguration;
    private GitRepositoryConfiguration gitOpsRepositoryConfiguration;
    private String artifactId;
    private String groupId;
    private String systemName;
    private String platform;
    private String department = "BIT";

    private List<TemplateParameter> templateParameters = new ArrayList<>();

    private List<TemplateModule> templateModules = new ArrayList<>();

    public Set<String> getAllModuleIds() {
        if (templateModules == null) {
            return Set.of();
        }
        return templateModules.stream()
                .map(TemplateModule::getId)
                .collect(toSet());
    }

    public TemplateModule getTemplateModule(String moduleId) {
        return templateModules.stream()
                .filter(module -> module.getId().equals(moduleId))
                .findFirst()
                .orElseThrow();
    }
}
