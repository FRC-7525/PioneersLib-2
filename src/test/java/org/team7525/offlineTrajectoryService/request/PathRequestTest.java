package org.team7525.offlineTrajectoryService.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.wpilib.math.geometry.Translation2d;

class PathRequestTest {
    @Test
    void clonePreservesAllRequestOptions() {
        PathRequest original =
                PathRequest.builder()
                        .segments(
                                java.util.List.of(
                                        PathRequestSegment.builder()
                                                .waypoints(
                                                        java.util.List.of(
                                                                PathWaypoint.from(
                                                                                new Translation2d(
                                                                                        1, 2))
                                                                        .build()))
                                                .build()))
                        .stopAtStart(false)
                        .stopAtEnd(false)
                        .targetDt(0.02)
                        .build();

        PathRequest copy = original.clone().build();

        assertEquals(original.segments, copy.segments);
        assertEquals(original.stopAtStart, copy.stopAtStart);
        assertEquals(original.stopAtEnd, copy.stopAtEnd);
        assertEquals(original.targetDt, copy.targetDt);
    }
}
