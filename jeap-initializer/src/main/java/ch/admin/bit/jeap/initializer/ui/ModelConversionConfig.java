package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.ui.model.ModuleConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.PlatformSelectionModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateSelectionModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.util.Base64;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ModelConversionConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(TemplateSelectionModel.class, new WizardStepModelFormatter<>(TemplateSelectionModel.class));
        registry.addFormatterForFieldType(PlatformSelectionModel.class, new WizardStepModelFormatter<>(PlatformSelectionModel.class));
        registry.addFormatterForFieldType(TemplateConfigurationModel.class, new WizardStepModelFormatter<>(TemplateConfigurationModel.class));
        registry.addFormatterForFieldType(ModuleConfigurationModel.class, new WizardStepModelFormatter<>(ModuleConfigurationModel.class));
    }

    @SuppressWarnings("NullableProblems")
    @RequiredArgsConstructor
    class WizardStepModelFormatter<T> implements Formatter<T> {

        private final Class<T> targetType;

        @Override
        public T parse(String base64, Locale locale) throws ParseException {
            try {
                byte[] jsonBytes = Base64.getDecoder().decode(base64);
                return objectMapper.readValue(new String(jsonBytes, UTF_8), targetType);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse JSON string to object", e);
                throw new ParseException(e.getMessage(), (int) e.getLocation().getCharOffset());
            }
        }

        @Override
        public String print(Object object, Locale locale) {
            try {
                byte[] jsonBytes = objectMapper.writeValueAsBytes(object);
                return Base64.getEncoder().encodeToString(jsonBytes);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize object to JSON string", e);
                throw new IllegalArgumentException(e);
            }
        }
    }
}
