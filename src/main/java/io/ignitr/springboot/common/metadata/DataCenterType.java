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
