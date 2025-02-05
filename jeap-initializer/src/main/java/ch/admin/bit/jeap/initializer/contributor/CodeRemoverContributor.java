package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * This Contributor removes all code blocks that should not be part of generated projects, for instance:
 * <pre>
 * // START INITIALIZER DELETE
 *  ...
 * // END INITIALIZER DELETE
 * </pre>
 * <pre>
 * &lt!-- START INITIALIZER DELETE -->
 * &lt!-- END INITIALIZER DELETE -->
 * </pre>
 */
@Component
public class CodeRemoverContributor implements ProjectContributor {

    private static final String BLOCK_START_PREFIX = "START ";
    private static final String BLOCK_END_PREFIX = "END ";

    private final String blockName;
    private final Pattern sourceFilesPattern;

    @Autowired
    public CodeRemoverContributor(JeapInitializerProperties properties) {
        this.blockName = "INITIALIZER DELETE";
        this.sourceFilesPattern = properties.getSourceFilesPattern();
    }

    public CodeRemoverContributor(String blockName, Pattern sourceFilesPattern) {
        this.blockName = blockName;
        this.sourceFilesPattern = sourceFilesPattern;
    }

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        FileUtils.removeRegionInFiles(sourceFilesPattern, projectRoot, BLOCK_START_PREFIX + blockName, BLOCK_END_PREFIX + blockName);
    }
}
