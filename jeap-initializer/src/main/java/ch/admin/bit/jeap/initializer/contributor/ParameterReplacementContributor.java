package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Project contributor that replaces parameters in source files.
 * <p>
 * Parameters are defined in the source files with the following pattern:
 * INITIALIZER PARAMETER &lt;parameter-name> VALUE &lt;parameter-value>
 * <p>
 * The parameter definition can be prefixed/postfixed with any string (ex. //, # or &lt;!--).
 * <p>
 * Lines with parameter definitions are removed from the source files and the parameters are replaced with their values.
 */
@Component
public class ParameterReplacementContributor implements ProjectContributor {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("INITIALIZER PARAMETER (.+) VALUE ([a-zA-Z0-9_\\-.]+)");

    private final Pattern sourceFilesPattern;

    public ParameterReplacementContributor(JeapInitializerProperties jeapInitializerProperties) {
        this.sourceFilesPattern = jeapInitializerProperties.getSourceFilesPattern();
    }

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) throws IOException {
        FileUtils.walkMatchingFiles(projectRoot, sourceFilesPattern, path -> replaceParameters(path, projectRequest));
    }

    @Override
    public int getOrder() {
        // We want this contributor to have a high priority as it operates rather locally on a part of a file. We would like
        // this contributor to make its contribution before more global contributors like the PropertyFilesContributor
        // (system name, artifact id, context path in configuration files) apply their contributions.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private void replaceParameters(Path path, ProjectRequest projectRequest) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(path));

        Map<String, String> params = new HashMap<>();
        List<String> updatedLines = lines.stream()
                .filter(line -> !matchParameterDefinitionLineExtractingParameters(line, params))
                .map(line -> replaceParams(line, params, projectRequest))
                .collect(Collectors.toCollection(ArrayList::new));

        if (!updatedLines.equals(lines)) {
            Files.write(path, updatedLines);
        }
    }

    private static boolean matchParameterDefinitionLineExtractingParameters(String line, Map<String, String> params) {
        var matcher = PARAMETER_PATTERN.matcher(line);

        boolean found = false;
        while (matcher.find()) {
            String parameterName = matcher.group(1);
            String parameterValue = matcher.group(2);
            params.put(parameterName, parameterValue);
            found = true;
        }
        return found;
    }

    private String replaceParams(String line, Map<String, String> params, ProjectRequest projectRequest) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String parameterName = entry.getKey();
            String valueToReplaceInFile = entry.getValue();
            String newValue = projectRequest.getParameterValue(parameterName);
            line = line.replace(valueToReplaceInFile, newValue);
        }
        return line;
    }
}
