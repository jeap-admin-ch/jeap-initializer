package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.api.InitializerController;
import ch.admin.bit.jeap.initializer.api.model.ProjectTemplateDTO;
import ch.admin.bit.jeap.initializer.api.model.TemplateModuleDTO;
import ch.admin.bit.jeap.initializer.model.Platform;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import ch.admin.bit.jeap.initializer.ui.model.ModuleConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.PlatformSelectionModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateSelectionModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/wizard")
public class WizardController {

    // View rendering attributes
    static final String PLATFORM_LIST = "platformList";
    static final String TEMPLATE_LIST = "templateList";
    static final String TEMPLATE_PARAMETERS = "templateParameters";
    static final String MODULE_LIST = "moduleList";
    // Models
    static final String PLATFORM_SELECTION_MODEL = "platformSelectionModel";
    static final String TEMPLATE_SELECTION_MODEL = "templateSelectionModel";
    static final String TEMPLATE_CONFIGURATION_MODEL = "templateConfigurationModel";
    static final String MODULE_CONFIGURATION_MODEL = "moduleConfigurationModel";

    private static final String FIRST_WIZARD_STEP_SELECT_PLATFORM = "wizard-step-select-platform";
    private static final String SECOND_WIZARD_STEP_SELECT_TEMPLATE = "wizard-step-select-template";
    private static final String THIRD_WIZARD_STEP_CONFIGURE_TEMPLATE = "wizard-step-configure-template";
    private static final String WIZARD_STEP_CONFIGURE_MODULES = "wizard-step-configure-modules";
    private static final String WIZARD_STEP_REVIEW = "wizard-step-review";

    private static final String REDIRECT_WIZARD_STEP_SELECT_PLATFORM = "redirect:/wizard/step/select-platform";
    private static final String REDIRECT_WIZARD_STEP_CONFIGURE_TEMPLATE = "redirect:/wizard/step/configure-template";
    private static final String REDIRECT_WIZARD_STEP_CONFIGURE_MODULES = "redirect:/wizard/step/configure-modules";
    private static final String REDIRECT_WIZARD_STEP_REVIEW = "redirect:/wizard/step/review";

    private final ProjectRequestFactory projectRequestFactory;
    private final TemplateService templateService;
    private final InitializerController initializerController;

    @GetMapping("/step/select-platform")
    public String showStepSelectPlatform(Model model) {
        preparePlatformSelectionModel(model);
        return FIRST_WIZARD_STEP_SELECT_PLATFORM;
    }

    private void preparePlatformSelectionModel(Model model) {
        List<Platform> platforms = templateService.getPlatforms();
        model.addAttribute(PLATFORM_LIST, platforms);
        if (!model.containsAttribute(PLATFORM_SELECTION_MODEL)) {
            model.addAttribute(PLATFORM_SELECTION_MODEL, new PlatformSelectionModel());
        }
    }

    @PostMapping("/step/select-platform")
    public String processStepSelectApp(@Valid @ModelAttribute(PLATFORM_SELECTION_MODEL) PlatformSelectionModel platformSelectionModel,
                                       BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showStepSelectPlatform(model);
        }

        redirectAttributes.addAttribute(PLATFORM_SELECTION_MODEL, platformSelectionModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, prepareTemplateSelectionModelForRendering(model, platformSelectionModel.getSelectedPlatformId()));
        return SECOND_WIZARD_STEP_SELECT_TEMPLATE;
    }

    @GetMapping("/step/select-template")
    public String showStepSelectTemplate(Model model, @ModelAttribute(PLATFORM_SELECTION_MODEL) PlatformSelectionModel platformSelectionModel,
                                         RedirectAttributes redirectAttributes) {
        var templateSelectionModel = prepareTemplateSelectionModelForRendering(model, platformSelectionModel.getSelectedPlatformId());
        redirectAttributes.addAttribute(PLATFORM_SELECTION_MODEL, platformSelectionModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        return SECOND_WIZARD_STEP_SELECT_TEMPLATE;
    }

    private TemplateSelectionModel prepareTemplateSelectionModelForRendering(Model model, String selectedPlatformKey) {
        List<ProjectTemplateDTO> projectTemplateDTOs = templateService.getProjectTemplatesForPlatform(selectedPlatformKey).stream()
                .map(ProjectTemplateDTO::from).toList();
        model.addAttribute(TEMPLATE_LIST, projectTemplateDTOs);

        if (!model.containsAttribute(TEMPLATE_SELECTION_MODEL)) {
            TemplateSelectionModel templateSelectionModel = new TemplateSelectionModel();
            model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
            return templateSelectionModel;
        } else {
            return (TemplateSelectionModel) model.getAttribute(TEMPLATE_SELECTION_MODEL);
        }
    }

    @PostMapping("/step/select-template")
    public String processStepSelectTemplate(@Valid @ModelAttribute(PLATFORM_SELECTION_MODEL) PlatformSelectionModel platformSelectionModel,
                                            @Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showStepSelectTemplate(model, platformSelectionModel, redirectAttributes);
        }

        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        return REDIRECT_WIZARD_STEP_CONFIGURE_TEMPLATE;
    }

    @GetMapping("/step/configure-template")
    public String showStepConfigureTemplate(@ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                            Model model) {
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);

        prepareTemplateConfigurationModelForRendering(model, templateSelectionModel);

        return THIRD_WIZARD_STEP_CONFIGURE_TEMPLATE;
    }

    @PostMapping("/step/configure-template")
    public String processStepConfigureTemplate(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                               @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                               BindingResult bindingResult, HttpServletRequest request,
                                               Model model, RedirectAttributes redirectAttributes) {
        var templateParameters = templateService.getTemplateParameters(templateSelectionModel.getSelectedTemplateId());
        templateParameters.forEach(templateParameter ->
                templateConfigurationModel.getTemplateParameterValues().put(templateParameter.getId(), request.getParameter(templateParameter.getId())));

        if (bindingResult.hasErrors()) {
            return showStepConfigureTemplate(templateSelectionModel, model);
        }

        redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        return REDIRECT_WIZARD_STEP_CONFIGURE_MODULES;
    }

    private void prepareTemplateConfigurationModelForRendering(Model model, TemplateSelectionModel templateSelectionModel) {
        String selectedTemplateId = templateSelectionModel.getSelectedTemplateId();
        model.addAttribute(TEMPLATE_PARAMETERS, templateService.getTemplateParameters(selectedTemplateId));
        model.addAttribute(MODULE_LIST, templateService.getProjectModules(selectedTemplateId)
                .stream().map(TemplateModuleDTO::from).toList());
        if (!model.containsAttribute(TEMPLATE_CONFIGURATION_MODEL)) {
            model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, new TemplateConfigurationModel());
        }
    }

    @GetMapping("/step/configure-modules")
    public String showStepConfigureModules(@RequestParam(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                           @RequestParam(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                           Model model, RedirectAttributes redirectAttributes) {

        var moduleParameters = templateService.getModuleParameters(
                templateSelectionModel.getSelectedTemplateId(), templateConfigurationModel.getSelectedModuleIds());

        if (moduleParameters.isEmpty()) {
            redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
            redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
            redirectAttributes.addAttribute(MODULE_CONFIGURATION_MODEL, new ModuleConfigurationModel());
            return REDIRECT_WIZARD_STEP_REVIEW;
        }

        prepareModuleConfigurationModelForRendering(model, moduleParameters);
        model.addAttribute(MODULE_CONFIGURATION_MODEL, new ModuleConfigurationModel());
        model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);

        return WIZARD_STEP_CONFIGURE_MODULES;
    }

    @PostMapping("/step/configure-modules")
    public String processStepConfigureModules(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                              @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                              @Valid @ModelAttribute(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel,
                                              BindingResult bindingResult, HttpServletRequest request,
                                              Model model, RedirectAttributes redirectAttributes) {

        var moduleParameters = templateService.getModuleParameters(
                templateSelectionModel.getSelectedTemplateId(), templateConfigurationModel.getSelectedModuleIds());
        moduleParameters.forEach(moduleParameter ->
                moduleConfigurationModel.getModuleParameterValues().put(moduleParameter.getId(), request.getParameter(moduleParameter.getId())));

        if (bindingResult.hasErrors()) {
            return showStepConfigureModules(templateSelectionModel, templateConfigurationModel, model, redirectAttributes);
        }

        redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        redirectAttributes.addAttribute(MODULE_CONFIGURATION_MODEL, moduleConfigurationModel);

        return REDIRECT_WIZARD_STEP_REVIEW;
    }

    private static void prepareModuleConfigurationModelForRendering(Model model, List<TemplateParameter> moduleParameters) {
        model.addAttribute("moduleParameters", moduleParameters);
    }

    @GetMapping("/step/review")
    public String review(@RequestParam(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                         @RequestParam(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                         @RequestParam(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel,
                         Model model) {
        model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        model.addAttribute(MODULE_CONFIGURATION_MODEL, moduleConfigurationModel);
        return WIZARD_STEP_REVIEW;
    }

    @PostMapping("/step/generate")
    public ResponseEntity<InputStreamResource> generate(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                                        @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                                        @Valid @ModelAttribute(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel) throws IOException {
        var projectRequest = projectRequestFactory.createProjectRequest(
                templateSelectionModel, templateConfigurationModel, moduleConfigurationModel);
        return initializerController.generate(projectRequest);
    }

    @PostMapping("/reset")
    public String reset() {
        return REDIRECT_WIZARD_STEP_SELECT_PLATFORM;
    }
}
