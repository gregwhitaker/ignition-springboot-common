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

package io.ignitr.springboot.common.metadata;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of supported datacenter types.
 */
public enum DataCenterType {
    IGNITR("Ignitr"),
    AMAZON("Amazon"),
    AMAZON_ECS("AmazonECS"),
    MYOWN("MyOwn");

    private static final Map<String, DataCenterType> lookup = new HashMap<>();

    static {
        for (DataCenterType t : EnumSet.allOf(DataCenterType.class)) {
            lookup.put(t.getValue(), t);
        }
    }

    private final String value;

    DataCenterType(final String value) {
        this.value = value;
    }

    /**
     * @return enumeration value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the enumerated type for the specified value.
     *
     * @param value enumeration value
     * @return the {@link DataCenterType} associated with the specified value or <code>null</code> if there is
     * no enumerated type associated with the specified value.
     */
    public static DataCenterType get(String value) {
        return lookup.get(value);
    }
}
