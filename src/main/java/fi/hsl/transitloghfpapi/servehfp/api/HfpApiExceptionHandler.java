package fi.hsl.transitloghfpapi.servehfp.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

@ControllerAdvice
public class HfpApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {HfpJobNotFinishedException.class})
    protected ResponseEntity<String> handleHfpNotReadyYetException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

}
