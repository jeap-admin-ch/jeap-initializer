package ch.admin.bit.jeap.initializer.template;

import ch.admin.bit.jeap.initializer.config.TemplateNotFoundException;
import ch.admin.bit.jeap.initializer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    private static final String PLATFORM_1 = "platform1";
    private static final String PLATFORM_2 = "platform2";
    public static final String PLATFORM_NAME_ALPHA = "Alpha";
    public static final String PLATFORM_NAME_BETA = "Beta";
    private static final Platform PLATFORM_INSTANCE_1 = new Platform(PLATFORM_1, PLATFORM_NAME_ALPHA, "");
    private static final Platform PLATFORM_INSTANCE_2 = new Platform(PLATFORM_2, PLATFORM_NAME_BETA, "");

    @Mock
    private TemplateRepository templateRepository;

    private TemplateService service;

    @BeforeEach
    void setUp() {
        service = new TemplateService(templateRepository);
    }

    @Test
    void getTemplate_returnsTemplate_whenExists() {
        ProjectTemplate tpl = projectTemplate("tpl1", "A Template", PLATFORM_1, "https://host/scm/platform1/repo.git");
        when(templateRepository.getTemplate("tpl1")).thenReturn(tpl);

        ProjectTemplate result = service.getTemplate("tpl1");
        assertThat(result).isSameAs(tpl);
    }

    @Test
    void getTemplate_throwsTemplateNotFound_whenMissing() {
        when(templateRepository.getTemplate("missing")).thenReturn(null);
        assertThrows(TemplateNotFoundException.class, () -> service.getTemplate("missing"));
    }

    @Test
    void getProjectPlatforms_returnsSortedByName() {
        ProjectTemplate a = projectTemplate("tplA", "Template A", PLATFORM_1, "https://host/scm/app/repoA.git");
        ProjectTemplate b = projectTemplate("tplB", "Template B", PLATFORM_2, "https://host/scm/app/repoB.git");
        ProjectTemplate c = projectTemplate("tplC", "Template C", PLATFORM_2, "https://host/scm/app/repoC.git");
        when(templateRepository.getTemplateKeys()).thenReturn(Set.of("tplB", "tplA", "tplC"));
        when(templateRepository.getTemplate("tplA")).thenReturn(a);
        when(templateRepository.getTemplate("tplB")).thenReturn(b);
        when(templateRepository.getTemplate("tplC")).thenReturn(c);
        when(templateRepository.getConfiguredPlatform(PLATFORM_1)).thenReturn(PLATFORM_INSTANCE_1);
        when(templateRepository.getConfiguredPlatform(PLATFORM_2)).thenReturn(PLATFORM_INSTANCE_2);

        List<Platform> result = service.getPlatforms();
        assertThat(result).extracting(Platform::name)
                .containsExactly(PLATFORM_NAME_ALPHA, PLATFORM_NAME_BETA);
    }

    @Test
    void getProjectTemplatesForPlatform_returnsSortedByName() {
        ProjectTemplate a = projectTemplate("tplA", "Template A", PLATFORM_1, "https://host/scm/app/repoA.git");
        ProjectTemplate b = projectTemplate("tplB", "Template B", PLATFORM_1, "https://host/scm/app/repoB.git");
        when(templateRepository.getTemplateKeys()).thenReturn(Set.of("tplB", "tplA"));
        when(templateRepository.getTemplate("tplA")).thenReturn(a);
        when(templateRepository.getTemplate("tplB")).thenReturn(b);

        List<ProjectTemplate> result = service.getProjectTemplatesForPlatform(PLATFORM_1);
        assertThat(result).extracting(ProjectTemplate::getName).containsExactly("Template A", "Template B");
    }

    @Test
    void getTemplateParameters_returnsFromTemplate() {
        TemplateParameter p1 = templateParameter("p1", "Param 1");
        TemplateParameter p2 = templateParameter("p2", "Param 2");
        ProjectTemplate tpl = projectTemplate("tplA", "Template A", PLATFORM_1, "https://host/scm/app/repoA.git");
        tpl.setTemplateParameters(List.of(p2, p1));
        when(templateRepository.getTemplate("tpl1")).thenReturn(tpl);

        List<TemplateParameter> params = service.getTemplateParameters("tpl1");
        assertThat(params).containsExactly(p2, p1);
    }

    @Test
    void getModuleParameters_filtersSelectedModules() {
        TemplateParameter pA = templateParameter("pa", "Alpha");
        TemplateParameter pC = templateParameter("pc", "Charlie");
        TemplateModule m1 = templateModule("m1", "Module 1", List.of(pA));
        TemplateModule m2 = templateModule("m2", "Module 2", List.of(pC));
        ProjectTemplate tpl = projectTemplate("tpl1", "Template", PLATFORM_1, "https://host/app/repo.git");
        tpl.setTemplateModules(List.of(m1, m2));
        when(templateRepository.getTemplate("tpl1")).thenReturn(tpl);

        Set<String> selected = new LinkedHashSet<>(Set.of("m2"));
        List<TemplateParameter> params = service.getModuleParameters("tpl1", selected);
        assertThat(params).extracting(TemplateParameter::getName).containsExactly("Charlie");
    }

    @Test
    void getProjectModules_returnsSortedByName() {
        TemplateModule m1 = templateModule("m1", "Module 1", List.of());
        TemplateModule m2 = templateModule("m2", "Module 2", List.of());
        ProjectTemplate tpl = projectTemplate("tpl1", "Template", PLATFORM_1, "https://host/app/repo.git");
        tpl.setTemplateModules(List.of(m1, m2));
        when(templateRepository.getTemplate("tpl1")).thenReturn(tpl);

        List<TemplateModule> modules = service.getProjectModules("tpl1");
        assertThat(modules).extracting(TemplateModule::getName).containsExactly("Module 1", "Module 2");
    }

    private ProjectTemplate projectTemplate(String key, String name, String platform, String repoUrl) {
        ProjectTemplate tpl = new ProjectTemplate();
        tpl.setKey(key);
        tpl.setName(name);
        tpl.setPlatform(platform);
        GitRepositoryConfiguration repoCfg = new GitRepositoryConfiguration();
        repoCfg.setUrl(repoUrl);
        tpl.setRepositoryConfiguration(repoCfg);
        tpl.setTemplateModules(new ArrayList<>());
        tpl.setTemplateParameters(new ArrayList<>());
        return tpl;
    }

    private TemplateParameter templateParameter(String id, String name) {
        TemplateParameter p = new TemplateParameter();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private TemplateModule templateModule(String id, String name, List<TemplateParameter> params) {
        TemplateModule m = new TemplateModule();
        m.setId(id);
        m.setName(name);
        m.setModuleParameters(params);
        return m;
    }
}
