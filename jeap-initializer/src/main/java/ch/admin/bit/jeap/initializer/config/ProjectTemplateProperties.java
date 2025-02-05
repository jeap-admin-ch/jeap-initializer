package ch.admin.bit.jeap.initializer.config;

import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;
import lombok.Data;

@Data
public class ProjectTemplateProperties {

    private GitRepositoryConfiguration repositoryConfiguration;
    private GitRepositoryConfiguration gitOpsRepositoryConfiguration;

}
