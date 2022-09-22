package common.models;

public class HealthCheckConfiguration {
    private int ConcurrentFailureThreshold = 3;
    private final int TimeoutThreshold = 5000;
    private int Wait = 5000;

    public int getWait() {
        return Wait;
    }

    public int getTimeoutThreshold() {
        return TimeoutThreshold;
    }

    public int getConcurrentFailureThreshold() {
        return ConcurrentFailureThreshold;
    }
}
