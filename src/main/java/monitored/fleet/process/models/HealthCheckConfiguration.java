package monitored.fleet.process.models;

//TODO: to be move to web.xml
public class HealthCheckConfiguration {
    private final int ConcurrentFailureThreshold = 3;
    private final int TimeoutThreshold = 5000;
    private final int Wait = 5000;

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
