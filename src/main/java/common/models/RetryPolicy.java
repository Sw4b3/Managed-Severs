package common.models;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryPolicy<T> {
    private final Supplier<T> Action;
    private final Predicate<T> Success;
    private final int Attempts;
    private boolean ExponentialBackOff = false;

    public RetryPolicy(Supplier<T> action, Predicate<T> success, int attempts) {
        Action = action;
        Success = success;
        Attempts = attempts;
    }

    public RetryPolicy(Supplier<T> action, Predicate<T> success, int attempts, boolean exponentialBackOff) {
        Action = action;
        Success = success;
        Attempts = attempts;
        ExponentialBackOff = exponentialBackOff;
    }

    public Supplier<T> getAction() {
        return Action;
    }

    public Predicate<T> getSuccess() {
        return Success;
    }

    public int getAttempts() {
        return Attempts;
    }

    public boolean getExponentialBackOff() {
        return ExponentialBackOff;
    }
}
