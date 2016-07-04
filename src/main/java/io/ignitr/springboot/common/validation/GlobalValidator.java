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

package io.ignitr.springboot.common.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator that is applied to all controllers within an Ignition compatible project.  This validator
 * allows users to implement the {@link ValidatorSupport} interface on POJOs they wish to have automatically
 * validated by a controller.
 */
@Component
public class GlobalValidator implements org.springframework.validation.Validator {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof ValidatorSupport) {
            Validator validator = ((ValidatorSupport) target).validator();

            if (validator != null) {
                beanFactory.autowireBean(validator);
                validator.validate(target, errors);
            }
        }
    }
}
