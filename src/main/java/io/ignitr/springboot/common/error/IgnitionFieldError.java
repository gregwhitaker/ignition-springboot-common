package io.ignitr.springboot.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Container for field-level error messages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "field",
        "code",
        "message"
})
public class IgnitionFieldError {
    private String field;
    private String code;
    private String message;

    /**
     * Creates a new field-level error without an error code.
     *
     * @param field field name
     * @param message error message
     */
    public IgnitionFieldError(final String field, final String message) {
        this(field, null, message);
    }

    /**
     * Creates a new field-level error.
     *
     * @param field field name
     * @param code error code
     * @param message error message
     */
    public IgnitionFieldError(final String field, final String code, final String message) {
        this.field = field;
        this.code = code;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
