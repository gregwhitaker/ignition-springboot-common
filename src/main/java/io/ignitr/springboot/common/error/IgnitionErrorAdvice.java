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

import io.ignitr.springboot.common.metadata.DeploymentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Adds a default error handler to all controllers.
 */
@ControllerAdvice
public class IgnitionErrorAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(IgnitionErrorAdvice.class);

    private final ErrorAttributes errorAttributes;

    @Autowired
    public IgnitionErrorAdvice(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Handles any unhandled exceptions raised by the application.
     *
     * @param httpRequest current http request
     * @param t           unhandled exception
     * @return default error response
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<IgnitionError> defaultExceptionHandler(HttpServletRequest httpRequest, Throwable t) {
        LOG.error("An error was caught by the default error handler", t);

        Map<String, Object> errorAttrs = parseErrorAttributes(httpRequest);

        IgnitionError body = new IgnitionError(
                getErrorAttributeValue(errorAttrs, "timestamp", String.class),
                getErrorAttributeValue(errorAttrs, "status", Integer.class),
                getErrorAttributeValue(errorAttrs, "code", String.class),
                getErrorAttributeValue(errorAttrs, "deploymentContext", DeploymentContext.class),
                getErrorAttributeValue(errorAttrs, "requestId", String.class),
                getErrorAttributeValue(errorAttrs, "requestPath", String.class),
                getErrorAttributeValue(errorAttrs, "message", String.class),
                getErrorAttributeValue(errorAttrs, "details", String.class)
        );

        if (errorAttrs.get("fieldErrors") != null) {
            body.setFieldErrors((List<IgnitionFieldError>) errorAttrs.get("fieldErrors"));
        }

        return ResponseEntity.status(getErrorAttributeValue(errorAttrs, "status", Integer.class))
                .body(body);
    }

    /**
     * Handles exceptions related to media types.
     *
     * @param httpRequest current http request
     * @param e           media type exception
     * @return default error response
     */
    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<IgnitionError> httpMediaTypeExceptionHandler(HttpServletRequest httpRequest, HttpMediaTypeException e) {
        Map<String, Object> errorAttrs = parseErrorAttributes(httpRequest);

        if (e instanceof HttpMediaTypeNotSupportedException) {
            errorAttrs.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        } else if (e instanceof HttpMediaTypeNotAcceptableException) {
            errorAttrs.put("status", HttpStatus.NOT_ACCEPTABLE.value());
        } else {
            throw new RuntimeException("Unhandled exception type encountered in httpMediaTypeExceptionHandler");
        }

        errorAttrs.put("message", HttpStatus.valueOf(Integer.parseInt(errorAttrs.get("status").toString())).getReasonPhrase());
        errorAttrs.put("details", e.getMessage());

        IgnitionError body = new IgnitionError(
                getErrorAttributeValue(errorAttrs, "timestamp", String.class),
                getErrorAttributeValue(errorAttrs, "status", Integer.class),
                getErrorAttributeValue(errorAttrs, "code", String.class),
                getErrorAttributeValue(errorAttrs, "deploymentContext", DeploymentContext.class),
                getErrorAttributeValue(errorAttrs, "requestId", String.class),
                getErrorAttributeValue(errorAttrs, "requestPath", String.class),
                getErrorAttributeValue(errorAttrs, "message", String.class),
                getErrorAttributeValue(errorAttrs, "details", String.class)
        );

        return ResponseEntity.status(getErrorAttributeValue(errorAttrs, "status", Integer.class))
                .body(body);
    }

    /**
     * Retrieves the error attributes from the request as a map.
     *
     * @param httpRequest current http request
     * @return error attributes map
     */
    private Map<String, Object> parseErrorAttributes(HttpServletRequest httpRequest) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(httpRequest);
        return errorAttributes.getErrorAttributes(requestAttributes, false);
    }

    /**
     * Retrieves a named value from the error attributes and casts it to the specified type.
     *
     * @param errorAttributes error attributes collection
     * @param name            attribute name
     * @param type            type to cast value
     * @param <T>             type to cast value
     * @return error attribute value cast to the desired type
     */
    private <T> T getErrorAttributeValue(Map<String, Object> errorAttributes, String name, Class<T> type) {
        return errorAttributes.get(name) != null ? type.cast(errorAttributes.get(name)) : null;
    }
}
