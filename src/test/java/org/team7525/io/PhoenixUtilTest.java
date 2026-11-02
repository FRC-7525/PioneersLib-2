package org.team7525.io;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PhoenixUtilTest {
    @Test
    void rejectsNonPositiveAttemptCounts() {
        assertThrows(IllegalArgumentException.class, () -> PhoenixUtil.tryUntilOk(0, () -> null));
        assertThrows(IllegalArgumentException.class, () -> PhoenixUtil.tryUntilOk(-1, () -> null));
    }
}
