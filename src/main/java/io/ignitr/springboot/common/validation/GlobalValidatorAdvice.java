package io.ignitr.springboot.common.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Adds the {@link GlobalValidator} to all controllers within the Ignition compatible project.
 */
@ControllerAdvice
public class GlobalValidatorAdvice {

    @Autowired
    private GlobalValidator globalValidator;

    @InitBinder
    public void bind(WebDataBinder binder) {
        binder.addValidators(globalValidator);
    }
}
