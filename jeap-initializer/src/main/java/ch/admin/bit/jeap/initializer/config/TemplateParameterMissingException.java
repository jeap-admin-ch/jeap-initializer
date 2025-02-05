package ch.admin.bit.jeap.initializer.config;

public class TemplateParameterMissingException extends RuntimeException {

    public TemplateParameterMissingException(String templateParameterName) {
        super("Required template parameter '%s' is missing".formatted(templateParameterName));
    }

}
