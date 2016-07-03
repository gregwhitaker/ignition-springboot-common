package io.ignitr.springboot.common.validation;

import org.springframework.validation.Errors;

/**
 * Interface that all custom validations executed by {@link GlobalValidator} must implement.
 *
 * @param <T> target type
 */
public interface Validator<T> {

    /**
     * Validates the supplied target object.
     *
     * @param target object to validate
     * @param errors errors collection to populate with validation errors
     */
    void validate(T target, Errors errors);
}
