package org.team7525.subsystem;

import java.util.Map;

public interface SubsystemStates {
    /** Enums with custom actions or values should override this accessor. */
    default State getState() {
        return new State(toString());
    }

    default String getStateString() {
        return getState().getStateString();
    }

    default <ValueType> ValueType getValue(String key, Class<ValueType> type) {
        Map<String, Object> dataStore = getState().getCustomFields();
        return type.cast(dataStore.get(key));
    }
}
