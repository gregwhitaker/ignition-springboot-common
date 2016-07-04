package io.ignitr.springboot.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.ignitr.springboot.common.metadata.DeploymentContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Common error message format returned by all Blueprint services.
 *
 * @author Greg Whitaker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "timestamp",
        "status",
        "code",
        "resource",
        "requestId",
        "requestPath",
        "message",
        "details",
        "fieldErrors"
})
public final class IgnitionError {
    private String timestamp;
    private int status;
    private String code;
    private Resource resource;
    private String requestId;
    private String requestPath;
    private String message;
    private String details;
    private List<IgnitionFieldError> fieldErrors;

    /**
     * Creates a new instance of a blueprint error message.
     *
     * @param timestamp error timestamp
     * @param status http status
     * @param code error code
     * @param deploymentContext application deployment context
     * @param requestId distributed trace identifier
     * @param requestPath request servlet path
     * @param message error message
     * @param details detailed error message
     */
    public IgnitionError(final String timestamp, final int status, final String code, final DeploymentContext deploymentContext,
                          final String requestId, final String requestPath, final String message, final String details) {
        this(timestamp, status, code, deploymentContext, requestId, requestPath, message, details, null);
    }

    /**
     * Creates a new instance of a blueprint error message.
     *
     * @param timestamp error timestamp
     * @param status http status
     * @param code error code
     * @param deploymentContext application deployment context
     * @param requestId distributed trace identifier
     * @param requestPath request servlet path
     * @param message error message
     * @param details detailed error message
     * @param fieldErrors field-level error messages
     */
    public IgnitionError(final String timestamp, final int status, final String code, final DeploymentContext deploymentContext,
                          final String requestId, final String requestPath, final String message, final String details,
                          final List<IgnitionFieldError> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.resource = new Resource(deploymentContext);
        this.requestId = requestId;
        this.requestPath = requestPath;
        this.message = message;
        this.details = details;
        this.fieldErrors = fieldErrors;
    }

    /**
     * Adds a field-level error message to this blueprint error message.
     *
     * @param fieldError field-level error message
     */
    public synchronized void addFieldError(IgnitionFieldError fieldError) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }

        this.fieldErrors.add(fieldError);
    }

    /**
     * Adds a field-level error message to this blueprint error message.
     *
     * @param field field name
     * @param message error message
     */
    public synchronized void addFieldError(final String field, final String message) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }

        this.fieldErrors.add(new IgnitionFieldError(field, message));
    }

    /**
     * Adds a field-level error message to this blueprint error message.
     *
     * @param field field name
     * @param code error code
     * @param message error message
     */
    public synchronized void addFieldError(final String field, final String code, final String message) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }

        this.fieldErrors.add(new IgnitionFieldError(field, code, message));
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<IgnitionFieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<IgnitionFieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    /**
     * Identifies a resource.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "datacenter",
            "environment",
            "region",
            "name",
            "version"
    })
    private class Resource implements Serializable {
        private static final long serialVersionUID = 6853164511036113109L;

        private String datacenter;
        private String environment;
        private String region;
        private String name;
        private String version;

        /**
         * Creates a new instance of a resource identifier.
         *
         * @param deploymentContext application deployment context
         */
        public Resource(DeploymentContext deploymentContext) {
            if (deploymentContext.getDatacenterType() != null) {
                this.datacenter = deploymentContext.getDatacenterType().getValue();
            }

            this.environment = deploymentContext.getEnvironment();
            this.region = deploymentContext.getRegion();
            this.name = deploymentContext.getName();
            this.version = deploymentContext.getVersion();
        }

        /**
         * Creates a new instance of a resource identifier.
         *
         * @param name application name
         * @param version application version
         */
        public Resource(String name, String version) {
            this(name, version, null, null, null);
        }

        /**
         * Creates a new instance of a resource identifier.
         *
         * @param name application name
         * @param version application version
         * @param datacenter deployment datacenter type
         * @param environment deployment environment
         * @param region deployment region
         */
        public Resource(String name, String version, String datacenter, String environment, String region) {
            this.datacenter = datacenter;
            this.environment = environment;
            this.region = region;
            this.name = name;
            this.version = version;
        }

        public String getDatacenter() {
            return datacenter;
        }

        public void setDatacenter(String datacenter) {
            this.datacenter = datacenter;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
