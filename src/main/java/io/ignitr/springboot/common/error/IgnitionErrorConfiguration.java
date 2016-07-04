package io.ignitr.springboot.common.error;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgnitionErrorConfiguration {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new IgnitionErrorAttributes();
    }
}
