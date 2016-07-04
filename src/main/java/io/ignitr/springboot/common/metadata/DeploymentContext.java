package io.ignitr.springboot.common.metadata;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds application metadata for reference at runtime.
 */
@Component
public class DeploymentContext {
    private static final String ENV_APP_NAME = "CLOUD_APP";
    private static final String ENV_APP_VERSION = "CLOUD_APP_VERSION";
    private static final String ENV_APP_DATACENTER = "CLOUD_DATACENTER";
    private static final String ENV_APP_ENVIRONMENT = "CLOUD_ENVIRONMENT";
    private static final String ENV_APP_REGION = "EC2_REGION";
    private static final String ENV_LOCAL = "local";
    private static final String DEFAULT_REGION = "us-west-2";
    private static final String DEFAULT_DATACENTER = "MyOwn";

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.version}")
    private String appVersion;

    @Value("${deployment.datacenter:}")
    private String datacenter;

    /**
     * @return application name
     */
    public String getName() {
        return appName != null ? appName : System.getenv(ENV_APP_NAME);
    }

    /**
     * @return application version
     */
    public String getVersion() {
        return appVersion != null ? appVersion : System.getenv(ENV_APP_VERSION);
    }

    /**
     * Returns the type of data center within which the application is currently deployed.
     *
     * <p>Valid values are:
     * <ul>
     *  <li>Amazon</li>
     *  <li>AmazonECS</li>
     *  <li>MyOwn</li>
     * </ul>
     *
     * @return data center type
     */
    public DataCenterType getDatacenterType() {
        if (StringUtils.isNotEmpty(datacenter)) {
            DataCenterType type = DataCenterType.get(datacenter);

            if (type == null) {
                StringBuilder message = new StringBuilder();
                message.append("The datacenter type '");
                message.append(type);
                message.append("' specified by the 'deployment.datacenter' property is not supported. ");
                message.append(" Supported types are: [");

                boolean first = true;
                for (DataCenterType t : DataCenterType.values()) {
                    if (!first) {
                        message.append(", ");
                    } else {
                        first = false;
                    }

                    message.append(t.getValue());
                }

                message.append("]");

                throw new IllegalArgumentException(message.toString());
            }

            return type;
        } else {
            return DataCenterType.get(System.getenv(ENV_APP_DATACENTER) != null ? System.getenv(ENV_APP_DATACENTER) : DEFAULT_DATACENTER);
        }
    }

    /**
     * @return deployment region
     */
    public String getRegion() {
        String region = System.getenv(ENV_APP_REGION);
        return region != null ? region : DEFAULT_REGION;
    }

    /**
     * @return deployment environment name
     */
    public String getEnvironment() {
        String env = System.getenv(ENV_APP_ENVIRONMENT);
        return env != null ? env : ENV_LOCAL;
    }

    @Override
    public String toString() {
        StringBuilder resource = new StringBuilder();

        if (getDatacenterType() != null) {
            switch (getDatacenterType()) {
                case IGNITR:
                    resource.append("ignitr");
                    break;
                case AMAZON:
                    resource.append("aws");
                    break;
                case AMAZON_ECS:
                    resource.append("awsecs");
                    break;
                default:
                    resource.append("myown");
            }

            resource.append(":");

            if (StringUtils.isNotEmpty(getEnvironment())) {
                resource.append(getEnvironment());
            }

            resource.append(":");

            if (StringUtils.isNotEmpty(getRegion())) {
                resource.append(getRegion());
            }

            resource.append(":");

            if (StringUtils.isNotEmpty(getName())) {
                resource.append(getName());
            }

            resource.append(":");

            if (StringUtils.isNotEmpty(getVersion())) {
                resource.append(getVersion());
            }
        } else {
            // Instance is running locally or in an unsupported data center configuration
            if (StringUtils.isNotEmpty(getName())) {
                resource.append(getName());
            }

            resource.append(":");

            if (StringUtils.isNotEmpty(getVersion())) {
                resource.append(getVersion());
            }
        }

        return resource.toString();
    }
}
