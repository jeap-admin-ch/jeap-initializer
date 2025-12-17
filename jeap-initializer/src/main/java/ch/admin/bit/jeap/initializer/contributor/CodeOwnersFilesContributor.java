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


/**
 * {@link ProjectContributor} that applies a GitHub/GitLab {@code CODEOWNERS} configuration to the generated project.
 * <p>
 * The contributor reads the desired {@code CODEOWNERS} file content from the {@link ProjectRequest}'s
 * template parameters using the key. If the parameter is present and not
 * empty, it will <strong>overwrite</strong> every file named {@value #CODE_OWNERS_FILE_NAME} found anywhere under the
 * project root (case-insensitive match on the file name).
 * <p>
 * If no matching file exists, a new {@code CODEOWNERS} file is created at {@code &lt;projectRoot&gt;/CODEOWNERS}.
 * <p>
 * If the request is {@code null}, the template parameters map is {@code null}, or the parameter is missing/empty,
 * this contributor performs no changes.
 *
 * <h2>Parameter format</h2>
 * The parameter value is written verbatim as the complete file content. This means callers are responsible for
 * providing correct {@code CODEOWNERS} syntax, for example:
 * <pre>
 * * @my-org/my-team
 * /docs/ @my-org/docs-team
 * </pre>
 *
 * <h2>Side effects</h2>
 * This contributor changes files on disk under {@code projectRoot}. It is intentionally destructive:
 * existing {@code CODEOWNERS} contents are replaced entirely (no merge).
 *
 * <h2>Ordering</h2>
 * Runs early in the generation pipeline ({@link Ordered#HIGHEST_PRECEDENCE} + 100) so subsequent contributors
 * that rely on the final repository layout can operate after the file is in place.
 */
@Slf4j
@Service
public class CodeOwnersFilesContributor implements ProjectContributor {

    private static final String CODE_OWNERS_FILE_NAME = "CODEOWNERS";
    private static final String CODE_OWNERS_ID = "codeOwners";
    private static final String CODE_OWNERS_NAME = "Application Code Owners";

    /**
     * Applies the {@code CODEOWNERS} configuration to the generated project.
     * <p>
     * Workflow:
     * <ol>
     *   <li>Reads the {@code codeOwners} template parameter from {@code projectRequest}.</li>
     *   <li>If the value is non-empty, overwrites all {@code CODEOWNERS} files under {@code projectRoot}
     *       (case-insensitive file-name match). If none exist, creates {@code projectRoot/CODEOWNERS}.</li>
     *   <li>If the value is missing/empty (or {@code projectRequest} is {@code null}), no files are modified.</li>
     * </ol>
     *
     * @param projectRoot    the root directory of the generated project; used as the base folder to search for existing
     *                       {@code CODEOWNERS} files and as the target location if a new file must be created.
     * @param projectRequest the request containing user input and template parameters; may be {@code null}.
     * @param template       the selected template; currently unused by this contributor but provided by the pipeline contract.
     * @implNote This method writes the parameter value as-is. It does not validate {@code CODEOWNERS} syntax and does
     * not append a trailing newline.
     */
    @Override
    public void contribute(@NonNull final Path projectRoot, final ProjectRequest projectRequest, final ProjectTemplate template) {
        final Optional<TemplateParameter> codeOwnersParameter = CollectionUtils.emptyIfNull(template.getTemplateParameters()).stream()
                .filter(templateParameter -> templateParameter.getId().equals(CODE_OWNERS_ID))
                .findFirst();
        if (codeOwnersParameter.isEmpty()) {
            log.warn("Code Owners [{}][{}] field is not in the Template. File [{}] won't be filled.",
                    CODE_OWNERS_ID, CODE_OWNERS_NAME, CODE_OWNERS_FILE_NAME);
            return;
        }

        // Get the Code Owners
        final String codeOwnersInputValue = getCodeOwnersFromProjectRequest(projectRequest);
        if (StringUtils.isNotEmpty(codeOwnersInputValue)) {
            overwriteFileContent(CODE_OWNERS_FILE_NAME, projectRoot, codeOwnersInputValue);
            log.info("File [{}] has be filled with code owners: [{}].",
                    CODE_OWNERS_FILE_NAME, codeOwnersInputValue);
        }
    }

    /**
     * Defines contributor execution priority.
     *
     * @return an order value that causes this contributor to run near the beginning of the contributor chain.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    /**
     * Extracts the {@code CODEOWNERS} content from the given request.
     * <p>
     * This method looks up {@value #CODE_OWNERS_ID} in {@link ProjectRequest#getTemplateParameters()}.
     *
     * @param projectRequest the project request; may be {@code null}
     * @return the configured {@code CODEOWNERS} content, or {@code null} if the request is {@code null} or the
     * parameter is not present.
     */
    private String getCodeOwnersFromProjectRequest(final ProjectRequest projectRequest) {
        if (projectRequest == null) {
            log.warn("Project Request is null. File [{}] won't be filled.", CODE_OWNERS_FILE_NAME);
            return null;
        }

        final Map<String, String> templateParameters = projectRequest.getTemplateParameters();
        return MapUtils.getString(templateParameters, CODE_OWNERS_ID);
    }
}
