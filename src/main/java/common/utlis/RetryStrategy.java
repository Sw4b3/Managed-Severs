package common.utlis;

import common.models.RetryPolicy;

public class RetryStrategy {
    private static final int[] exponentialSequence = new int[]{1, 1, 5, 10, 30};

    public static <T> T execute(RetryPolicy<T> policy) {
        int i = 1;

        T result = policy.getAction().get();

        var isIndefinite = policy.getAttempts() == -1;

        while (policy.getSuccess().test(result)) {
            try {
                Thread.sleep(policy.getExponentialBackOff() ? getExponentialBackOff(i) : 1000);

                if (i++ >= policy.getAttempts() && !isIndefinite)
                    return result;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            result = policy.getAction().get();
        }

        return result;
    }

    public static int getExponentialBackOff(int attempts) {
        return exponentialSequence.length <= attempts
                ? exponentialSequence[exponentialSequence.length - 1] * 1000
                : exponentialSequence[attempts] * 1000;
    }
}
