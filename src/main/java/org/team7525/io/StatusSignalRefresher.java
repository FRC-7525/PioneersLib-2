package org.team7525.io;

import com.ctre.phoenix6.StatusSignal;
import java.util.*;

public class StatusSignalRefresher {
    private static StatusSignalRefresher instance;

    public static StatusSignalRefresher getInstance() {
        if (instance == null) {
            instance = new StatusSignalRefresher();
        }
        return instance;
    }

    private final Map<Integer, List<StatusSignal<?>>> statusSignalMap;
    private final Map<Integer, String> calculatedStatusSignalsPerCycle;
    private final Map<String, StatusSignal<?>[]> calculatedStatusSignalsArrays;
    private int _cycleCount;

    private StatusSignalRefresher() {
        statusSignalMap = new HashMap<>();
        calculatedStatusSignalsPerCycle = new HashMap<>();
        calculatedStatusSignalsArrays = new HashMap<>();
    }

    /**
     * Adds status signals to refresh every loop.
     *
     * @param signals The signals to refresh every loop
     */
    public void addStatusSignals(StatusSignal<?>... signals) {
        addStatusSignals(0, signals);
    }

    /**
     * Adds status signals to refresh, delaying by N loops between refreshes. 0 is every loop, 1 is
     * every other loop, 2 is every third loop, etc.
     *
     * @param delayLoopCount The amount of loops between refreshes
     * @param signals The signals to refresh
     */
    public void addStatusSignals(int delayLoopCount, StatusSignal<?>... signals) {
        if (!statusSignalMap.containsKey(delayLoopCount)) {
            statusSignalMap.put(delayLoopCount, new ArrayList<>());
        }
        var statusSignalList = statusSignalMap.get(delayLoopCount);
        Collections.addAll(statusSignalList, signals);
    }

    /** Finalizes all status signals. */
    public void finalizeStatusSignals() {
        calculatedStatusSignalsPerCycle.clear();
        calculatedStatusSignalsArrays.clear();
        var allStatusSignalKeys = statusSignalMap.keySet();
        var statusSignalDelayCounts =
                allStatusSignalKeys.stream()
                        .filter(a -> a != 0)
                        .sorted()
                        .toList()
                        .toArray(new Integer[0]);
        if (statusSignalDelayCounts.length > 0) {
            var countedValues = statusSignalDelayCounts.clone();
            int largestNumber =
                    countedValues[countedValues.length - 1]; // Largest value at last index to start
            // If all numbers are the same we're good -- countedValues[0] will be the same as all
            // other values,
            // otherwise we still need to iterate
            // Only reason we don't use largestNumber is due to variable access in lambdas. Yay
            // Java.
            while (!Arrays.stream(countedValues).allMatch(a -> a.equals(countedValues[0]))) {
                for (int i = 0; i < countedValues.length; i++) {
                    while (largestNumber > countedValues[i]) {
                        countedValues[i] += statusSignalDelayCounts[i];
                    }
                    largestNumber = countedValues[i];
                }
            }
            // Keep toRefresh around for all iterations to reduce GC pressure
            var toRefresh = new ArrayList<StatusSignal<?>>();
            for (int i = 0; i < largestNumber; i++) {
                var sb = new StringBuilder();
                for (var key : statusSignalMap.keySet()) {
                    if (i % (key + 1) == 0) {
                        toRefresh.addAll(statusSignalMap.get(key));
                        if (!sb.isEmpty()) {
                            sb.append(",");
                        }
                        sb.append(key);
                    }
                }
                var keyPattern = sb.toString();
                calculatedStatusSignalsPerCycle.put(i, keyPattern);
                if (!calculatedStatusSignalsArrays.containsKey(keyPattern)) {
                    calculatedStatusSignalsArrays.put(
                            keyPattern, toRefresh.toArray(new StatusSignal[0]));
                }
                toRefresh.clear();
            }
        } else {
            calculatedStatusSignalsPerCycle.put(0, "0");
            calculatedStatusSignalsArrays.put(
                    "0", statusSignalMap.get(0).toArray(new StatusSignal[0]));
        }
    }

    /**
     * Refreshes all relevant status signals. Must be called exactly once per robot loop, preferably
     * in robotPeriodic() before all other periodics are run.
     */
    public void refreshStatusSignals() {
        var calculatedCount = calculatedStatusSignalsPerCycle.size();
        StatusSignal<?>[] toRefresh;
        if (calculatedCount <= 1) {
            toRefresh = calculatedStatusSignalsArrays.get("0");
        } else {
            var statusSignalArrayKey =
                    calculatedStatusSignalsPerCycle.get(_cycleCount % (calculatedCount - 1));
            toRefresh = calculatedStatusSignalsArrays.get(statusSignalArrayKey);
        }
        StatusSignal.refreshAll(toRefresh);
        _cycleCount++;
    }
}
