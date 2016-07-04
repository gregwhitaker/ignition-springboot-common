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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.cloud.sleuth.Span;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Default error attributes builder for all ignition compliant applications.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IgnitionErrorAttributes implements ErrorAttributes, HandlerExceptionResolver, Ordered {
    private static final String ERROR_ATTRIBUTE = IgnitionErrorAttributes.class.getName() + ".ERROR";
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    @Autowired
    private DeploymentContext deploymentContext;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        storeErrorAttributes(request, ex);
        return null;
    }

    private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
        request.setAttribute(ERROR_ATTRIBUTE, ex);
    }

    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new HashMap<>();
        addTimestamp(errorAttributes);
        addStatus(errorAttributes, requestAttributes);
        addCode(errorAttributes, requestAttributes);
        addDeploymentContext(errorAttributes);
        addTraceId(errorAttributes);
        addPath(errorAttributes, requestAttributes);
        addDetailedMessage(errorAttributes, requestAttributes);
        addFieldErrors(errorAttributes, requestAttributes);

        return errorAttributes;
    }

    @Override
    public Throwable getError(RequestAttributes requestAttributes) {
        Throwable exception = getAttribute(requestAttributes, ERROR_ATTRIBUTE);
        if (exception == null) {
            exception = getAttribute(requestAttributes, "javax.servlet.error.exception");
        }
        return exception;
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }

    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return (BindingResult) error;
        }
        if (error instanceof MethodArgumentNotValidException) {
            return ((MethodArgumentNotValidException) error).getBindingResult();
        }
        return null;
    }

    /**
     * Adds the timestamp to the error response.
     *
     * @param errorAttributes error attributes collection
     */
    private void addTimestamp(Map<String, Object> errorAttributes) {
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT);
        sdf.setTimeZone(UTC_TIMEZONE);

        errorAttributes.put("timestamp", sdf.format(new Date()));
    }

    /**
     * Adds the http status to the error response.
     *
     * @param errorAttributes error attributes collection
     * @param requestAttributes request attributes collection
     */
    private void addStatus(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        Integer status = getAttribute(requestAttributes, "javax.servlet.error.status_code");
        Throwable error = getError(requestAttributes);

        if (error instanceof IgnitionException) {
            // If the error is an ignition compatible error we need to get the status code from the exception
            status = ((IgnitionException) error).getHttpStatus().value();
        } else if (status == null && error instanceof MethodArgumentNotValidException) {
            // If a validation error occurs default to 400 - Bad Request
            status = 400;
        } else {
            // If a status cannot be found default to 500 - Internal Server Error
            status = 500;
        }

        errorAttributes.put("status", status);

        try {
            errorAttributes.put("message", HttpStatus.valueOf(status).getReasonPhrase());
        } catch (Exception ex) {
            // Unable to obtain a reason
            errorAttributes.put("message", "Http Status " + status);
        }
    }

    /**
     * Adds the error code to the error response.
     *
     * @param errorAttributes error attributes collection
     * @param requestAttributes request attributes collection
     */
    private void addCode(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        Throwable error = getError(requestAttributes);

        if (error instanceof IgnitionException) {
            if (((IgnitionException) error).getErrorCode() != null) {
                errorAttributes.put("code", ((IgnitionException) error).getErrorCode().getValue());
            } else {
                errorAttributes.put("code", "UNKNOWN");
            }
        } else {
            // Adds the error code for an unhandled exception if a code was not present
            errorAttributes.put("code", "99999");
        }
    }

    /**
     * Adds the resource identifier to the error response.
     *
     * @param errorAttributes error attributes collection
     */
    private void addDeploymentContext(Map<String, Object> errorAttributes) {
        errorAttributes.put("deploymentContext", deploymentContext);
    }

    /**
     * Adds the distributed trace identifier to the error response.
     *
     * @param errorAttributes error attributes collection
     */
    private void addTraceId(Map<String, Object> errorAttributes) {
        String traceId = MDC.get(Span.TRACE_ID_NAME);

        if (StringUtils.isNotEmpty(traceId)) {
            errorAttributes.put("requestId", traceId);
        } else {
            errorAttributes.put("requestId", "UNKNOWN");
        }
    }

    /**
     * Adds the request path to the error response.
     *
     * @param errorAttributes error attributes collection
     * @param requestAttributes request attributes collection
     */
    private void addPath(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        String path = getAttribute(requestAttributes, "javax.servlet.error.request_uri");
        if (path != null) {
            errorAttributes.put("requestPath", path);
        }
    }

    /**
     * Adds the error message to the error response.
     *
     * @param errorAttributes error attributes collection
     */
    private void addDetailedMessage(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        Throwable error = getError(requestAttributes);
        if (error == null) {
            errorAttributes.put("details", null);
            return;
        }

        BindingResult result = extractBindingResult(error);
        if (result == null) {
            // Don't divulge detailed error information on unhandled exceptions
            if (error instanceof IgnitionException) {
                errorAttributes.put("details", error.getMessage());
            }

            return;
        }

        if (result.getErrorCount() > 0) {
            errorAttributes.put("details", "Validation failed for '" + result.getObjectName()
                    + "'. Error count: " + result.getErrorCount());
        } else {
            // Don't divulge detailed error information on unhandled exceptions
            if (error instanceof IgnitionException) {
                errorAttributes.put("details", error.getMessage());
            }
        }
    }

    /**
     * Adds field-level error messages to the error response.
     *
     * @param errorAttributes error attributes collection
     * @param requestAttributes request attributes collection
     */
    private void addFieldErrors(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        Throwable error = getError(requestAttributes);

        // Checking to see if the exception contains JSR-303 bean validation errors
        BindingResult result = extractBindingResult(error);
        if (result != null) {
            if (result.getErrorCount() > 0) {
                List<IgnitionFieldError> fieldErrors = new ArrayList<>(result.getErrorCount());

                for (ObjectError objError : result.getAllErrors()) {
                    String field = ((FieldError) objError).getField();
                    String message = objError.getDefaultMessage();

                    fieldErrors.add(new IgnitionFieldError(field, message));
                }

                errorAttributes.put("fieldErrors", fieldErrors);
            }
        }

        // Checking to see if the exception is an ignition compatible exception and thus could
        // potentially contain field-level errors
        if (error instanceof IgnitionException) {
            if (((IgnitionException) error).hasFieldErrors()) {
                errorAttributes.put("fieldErrors", ((IgnitionException) error).getFieldErrors());
            }
        }
    }
}
