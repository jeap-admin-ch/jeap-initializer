package ch.admin.bit.jeap.initializer.config;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateKey) {
        super("No template found with key " + templateKey);
    }

}
