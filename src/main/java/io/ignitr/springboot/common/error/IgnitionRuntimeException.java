/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ignitr.springboot.common.error;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Base exception that ignition compliant exceptions may subclass in order to get support for error codes.
 */
public class IgnitionRuntimeException extends RuntimeException implements IgnitionException {
    private HttpStatus httpStatus;
    private IgnitionErrorCode errorCode;
    private List<IgnitionFieldError> fieldErrors;

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     */
    public IgnitionRuntimeException() {
        //Noop
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param message error message
     */
    public IgnitionRuntimeException(final String message) {
        super(message);
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param message error message
     * @param errorCode error code
     */
    public IgnitionRuntimeException(final String message, final IgnitionErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param httpStatus http status code
     * @param message error message
     */
    public IgnitionRuntimeException(final HttpStatus httpStatus, final String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param httpStatus http status code
     * @param message error message
     */
    public IgnitionRuntimeException(final int httpStatus, final String message) {
        super(message);
        this.httpStatus = HttpStatus.valueOf(httpStatus);
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param httpStatus http status code
     * @param message error message
     * @param errorCode error code
     */
    public IgnitionRuntimeException(final HttpStatus httpStatus, final String message, final IgnitionErrorCode errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Initializes this instance of {@link IgnitionRuntimeException}.
     *
     * @param httpStatus http status code
     * @param message error message
     * @param errorCode error code
     */
    public IgnitionRuntimeException(final int httpStatus, final String message, final IgnitionErrorCode errorCode) {
        super(message);
        this.httpStatus = HttpStatus.valueOf(httpStatus);
        this.errorCode = errorCode;
    }

    @Override
    public void addFieldError(final String field, final String message) {
        if (fieldErrors == null) {
            this.fieldErrors = new ArrayList<>();
        }

        this.fieldErrors.add(new IgnitionFieldError(field, message));
    }

    @Override
    public void addFieldError(final String field, final IgnitionErrorCode errorCode, final String message) {
        if (fieldErrors == null) {
            this.fieldErrors = new ArrayList<>();
        }

        this.fieldErrors.add(new IgnitionFieldError(field, errorCode.getValue(), message));
    }

    @Override
    public boolean hasFieldErrors() {
        if (fieldErrors != null) {
            return fieldErrors.size() > 0;
        }

        return false;
    }

    @Override
    public HttpStatus getHttpStatus() {
        if (httpStatus == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return httpStatus;
        }
    }

    @Override
    public IgnitionErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public List<IgnitionFieldError> getFieldErrors() {
        return fieldErrors;
    }
}
