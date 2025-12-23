package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static ch.admin.bit.jeap.initializer.util.FileUtils.overwriteFileContent;


@Slf4j
@Service
public class CodeOwnersFilesContributor implements ProjectContributor {

    private static final String CODE_OWNERS_FILE_NAME = "CODEOWNERS";
    private static final String CODE_OWNERS_ID = "codeOwners";
    private static final String CODE_OWNERS_NAME = "Application Code Owners";

    @Override
    public void contribute(@NonNull final Path projectRoot, final ProjectRequest projectRequest, final ProjectTemplate template) {
        Optional<TemplateParameter> codeOwnersParameter = CollectionUtils.emptyIfNull(template.getTemplateParameters()).stream()
                .filter(templateParameter -> templateParameter.getId().equals(CODE_OWNERS_ID))
                .findFirst();
        if (codeOwnersParameter.isEmpty()) {
            log.warn("Code Owners [{}][{}] field is not in the Template. File [{}] won't be filled.",
                    CODE_OWNERS_ID, CODE_OWNERS_NAME, CODE_OWNERS_FILE_NAME);
            return;
        }

        // Get the Code Owners
        String codeOwnersInputValue = getCodeOwnersFromProjectRequest(projectRequest);
        if (StringUtils.isNotEmpty(codeOwnersInputValue)) {
            overwriteFileContent(CODE_OWNERS_FILE_NAME, projectRoot, codeOwnersInputValue);
            log.info("File [{}] has be filled with code owners: [{}].",
                    CODE_OWNERS_FILE_NAME, codeOwnersInputValue);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private String getCodeOwnersFromProjectRequest(final ProjectRequest projectRequest) {
        if (projectRequest == null) {
            log.warn("Project Request is null. File [{}] won't be filled.", CODE_OWNERS_FILE_NAME);
            return null;
        }

        Map<String, String> templateParameters = projectRequest.getTemplateParameters();
        return MapUtils.getString(templateParameters, CODE_OWNERS_ID);
    }
}
