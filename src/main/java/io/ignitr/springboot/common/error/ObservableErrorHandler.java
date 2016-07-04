package io.ignitr.springboot.common.error;

import com.google.common.base.Throwables;
import io.ignitr.springboot.common.metadata.DeploymentContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class that can be used to convert a {@link Throwable} encountered while subscribing
 * to an {@link rx.Observable} into a {@link IgnitionError}.
 */
@Component
public class ObservableErrorHandler {
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    private final DeploymentContext deploymentContext;
    private final Tracer tracer;

    @Autowired
    public ObservableErrorHandler(DeploymentContext deploymentContext, Tracer tracer) {
        this.deploymentContext = deploymentContext;
        this.tracer = tracer;
    }

    /**
     * Transforms a {@link Throwable} for a Request into a {@link IgnitionError}.
     *
     * @param request   http servlet request
     * @param throwable the Observable onError throwable
     * @return {@link ResponseEntity} of {@link IgnitionError}
     */
    public ResponseEntity<IgnitionError> handleError(HttpServletRequest request, Throwable throwable) {
        Throwable rootCause = Throwables.getRootCause(throwable);
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT);
        sdf.setTimeZone(UTC_TIMEZONE);

        // Fields and defaults that make up the ignition error response object
        Integer status = null;
        String timestamp = sdf.format(new Date());
        String code = "99999";
        String requestId = "UNKNOWN";

        if (rootCause instanceof IgnitionException) {
            IgnitionException error = (IgnitionException) rootCause;

            // Set the status from the BlueprintException implementation
            status = error.getHttpStatus().value();

            // Set the error code from the BlueprintException implementation
            if (error.getErrorCode() != null) {
                code = error.getErrorCode().getValue();
            } else {
                code = "UNKNOWN";
            }
        } else if (status == null && rootCause instanceof MethodArgumentNotValidException) {
            // If a validation error occurs default to 400 - Bad Request
            status = 400;
        } else {
            // If a status cannot be found default to 500 - Internal Server Error
            status = 500;
        }

        // Use the tracing id for the request id, when available
        String traceId = Span.idToHex(tracer.getCurrentSpan().getTraceId());
        if (StringUtils.isNotEmpty(traceId)) {
            requestId = traceId;
        }

        return new ResponseEntity<>(
                new IgnitionError(timestamp,
                        status,
                        code,
                        deploymentContext,
                        requestId,
                        request.getRequestURI(),
                        HttpStatus.valueOf(status).getReasonPhrase(),
                        getDetails(rootCause),
                        getFieldErrors(rootCause)),
                HttpStatus.valueOf(status)
        );
    }

    /**
     * Retrieves the detailed error message for the {@link Throwable}.
     *
     * @param error the throwable
     * @return {@link String} or null if none available
     */
    private String getDetails(Throwable error) {
        BindingResult result = extractBindingResult(error);
        if (result == null) {
            // Don't divulge detailed error information on unhandled exceptions
            if (error instanceof IgnitionException) {
                return error.getMessage();
            }

            return null;
        }

        if (result.getErrorCount() > 0) {
            return "Validation failed for '" + result.getObjectName() + "'. Error count: " + result.getErrorCount();
        } else {
            // Don't divulge detailed error information on unhandled exceptions
            if (error instanceof IgnitionException) {
                return error.getMessage();
            }
        }

        return null;
    }

    /**
     * Retrieves a {@link List} of {@link IgnitionFieldError} for the {@link Throwable}.
     *
     * @param error the throwable
     * @return {@link List} or null if none available
     */
    private List<IgnitionFieldError> getFieldErrors(Throwable error) {
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

                return fieldErrors;
            }
        }

        // Checking to see if the exception is an ignition compatible exception and thus could
        // potentially contain field-level errors
        if (error instanceof IgnitionException) {
            if (((IgnitionException) error).hasFieldErrors()) {
                return ((IgnitionException) error).getFieldErrors();
            }
        }

        return null;
    }

    /**
     * Attempts to find the {@link BindingResult} for the {@link Throwable}.
     *
     * @param error the throwable
     * @return {@link BindingResult} or null if not found
     */
    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return (BindingResult) error;
        }
        if (error instanceof MethodArgumentNotValidException) {
            return ((MethodArgumentNotValidException) error).getBindingResult();
        }
        return null;
    }
}
