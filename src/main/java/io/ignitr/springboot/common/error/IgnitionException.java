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

import java.util.List;

/**
 * Interface that all Ignition compatible exceptions must implement.
 */
public interface IgnitionException {

    /**
     * Adds a field-level error to this exception.
     *
     * @param field field name
     * @param message field error message
     */
    void addFieldError(final String field, final String message);

    /**
     * Adds a field-level error to this exception.
     *
     * @param field field name
     * @param errorCode field error code
     * @param message field error message
     */
    void addFieldError(final String field, final IgnitionErrorCode errorCode, final String message);

    /**
     * Indicates whether or not this exception contains field-level errors.
     *
     * @return <code>true</code> if this exception contains field-level errors; otherwise <code>false</code>
     */
    boolean hasFieldErrors();

    /**
     * @return http status code
     */
    HttpStatus getHttpStatus();

    /**
     * @return error code
     */
    IgnitionErrorCode getErrorCode();

    /**
     * @return field-level errors if any exist for this exception
     */
    List<IgnitionFieldError> getFieldErrors();
}
