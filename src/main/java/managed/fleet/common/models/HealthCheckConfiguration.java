package managed.fleet.common.models;

public class HealthCheckConfiguration {
    private int Attempts = 5;
    private int ConcurrentFailureThreshold = 5;
    private final int TimeoutThreshold = 5000;
    private int Wait = 5000;

    public int getAttempts(){
        return Attempts;
    }

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
