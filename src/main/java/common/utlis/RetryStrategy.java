package common.utlis;

import common.models.RetryPolicy;
import org.springframework.lang.Nullable;

public class RetryStrategy {
    private static final int[] fibonacci = new int[]{1, 1, 2, 3, 5, 8, 13};

    public static <T> T execute(RetryPolicy<T> policy) {
        int i = 1;

        T result = policy.getAction().get();

        var isIndefinite = policy.getAttempts() == -1;

        while (policy.getSuccess().test(result)) {
            if (!isIndefinite && i++ >= policy.getAttempts())
                return result;

            try {
                Thread.sleep(policy.getExponentialBackOff() ? fibonacci[policy.getAttempts()] * 1000 : 1000);

            } catch (InterruptedException e) {
            }

            result = policy.getAction().get();
        }

        return result;
    }
}
