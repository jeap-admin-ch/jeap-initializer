package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Inspired by io.spring.initializr.generator.project.contributor.ProjectContributor
 */
@FunctionalInterface
public interface ProjectContributor extends Ordered {

    /**
     * This method will be called after the initial code has been checked out from Git. Your custom logic has to be
     * implemented here and should do the required changes to files and folders under 'projectRoot'
     * <p>
     * The ordering of execution can be set by overriding the getOrder() method.
     *
     * @param projectRoot a Path containing the root project folder
     * @param projectRequest the project request containing the input data
     * @param template the project template
     */
    void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) throws IOException;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
