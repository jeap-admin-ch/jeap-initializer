package ch.admin.bit.jeap.initializer.api;

import ch.admin.bit.jeap.initializer.config.TemplateModuleNotFoundException;
import ch.admin.bit.jeap.initializer.config.TemplateNotFoundException;
import ch.admin.bit.jeap.initializer.config.TemplateParameterMissingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestResponseExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<String> handleTemplateNotFoundException(TemplateNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TemplateParameterMissingException.class)
    public ResponseEntity<String> handleTemplateParameterNotFoundException(TemplateParameterMissingException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TemplateModuleNotFoundException.class)
    public ResponseEntity<String> handleTemplateModuleNotFoundException(TemplateModuleNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
