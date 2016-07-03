package io.ignitr.springboot.common.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator that is applied to all controllers within an Ignition compatible project.  This validator
 * allows users to implement the {@link ValidatorSupport} interface on POJOs they wish to have automatically
 * validated by a controller.
 */
@Component
public class GlobalValidator implements org.springframework.validation.Validator {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof ValidatorSupport) {
            Validator validator = ((ValidatorSupport) target).validator();

            if (validator != null) {
                beanFactory.autowireBean(validator);
                validator.validate(target, errors);
            }
        }
    }
}
