package io.ignitr.springboot.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Monitors the health of the service and returns an appropriate status code based upon the health.
 *
 * <p>
 * The health check is run in a separate thread on a scheduled basis to prevent frequent calls to the health
 * check endpoint from affecting the overall performance of the service.
 * </p>
 *
 * <p>
 * For more information on creating HealthIndicators please refer to section 45.6.2 "Writing Custom HealthIndicators" of
 * the Spring Boot documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html
 * </p>
 */
public abstract class IgnitionHealthCheck implements HealthIndicator {
    private final Timer timer = new Timer("HealthCheckTaskTimer", true);
    private volatile Health currentHealth = Health.outOfService().build();

    /**
     * Initializes a default instance with a health check timer that will run the {@code IgnitionHealthCheck.doHealthCheck()}
     * method every 10 seconds.
     */
    public IgnitionHealthCheck() {
        this(0L, 10000L);
    }

    /**
     * Initializes a health check timer that will run the {@code IgnitionHealthCheck.doHealthCheck()} code at the
     * specified interval.
     *
     * @param delay number of milliseconds to delay the initial start of the health check task
     * @param period number of milliseconds in between each invocation of the health check logic in the {@code IgnitionHealthCheck.execute()} method
     */
    public IgnitionHealthCheck(long delay, long period) {
        timer.schedule(new HealthCheckTask(), delay, period);
    }

    /**
     * Executes the logic that will determine if the service is healthy or not.
     *
     * @return service health
     */
    public abstract Health doHealthCheck();

    @Override
    public Health health() {
        return currentHealth;
    }

    /**
     * Task that determines the health of the service.
     */
    private class HealthCheckTask extends TimerTask {

        @Override
        public void run() {
            currentHealth = doHealthCheck();
        }
    }
}
