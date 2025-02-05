package ch.admin.bit.jeap.initializer.template;

public class TemplateException extends RuntimeException {
    private TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    static TemplateException templateLoadingFailed(Throwable cause) {
        return new TemplateException("Template loading failed", cause);
    }
}
