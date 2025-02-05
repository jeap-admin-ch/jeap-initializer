package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This contributor modifies pom.xml files to reflect the group id
 */
@Slf4j
@Component
public class GroupIdRenamerContributor implements ProjectContributor {
    
    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        if (isBlank(template.getGroupId()) || isBlank(projectRequest.getGroupId())) {
            return;
        }

        FileUtils.replaceInFiles("pom.xml", projectRoot, "<groupId>" + template.getGroupId() + "</groupId>", "<groupId>" + projectRequest.getGroupId() + "</groupId>");
    }

}
