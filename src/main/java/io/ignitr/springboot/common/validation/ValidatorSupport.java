package io.ignitr.springboot.common.validation;

/**
 * Interface that all beans supporting automatic validation via {@link GlobalValidator} must implement.
 *
 * @param <T> type to be validated
 */
public interface ValidatorSupport<T> {

    /**
     * @return {@link Validator} instance responsible for determining if the implementing class is valid
     */
    Validator<T> validator();
}
