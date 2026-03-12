import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DistributedRateLimiter {
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillRatePerMillis;
        private double currentTokens;
        private long lastRefillTimestamp;

        public TokenBucket(long maxTokens, long refillIntervalMillis) {
            this.maxTokens = maxTokens;
            this.refillRatePerMillis = maxTokens / refillIntervalMillis;
            this.currentTokens = maxTokens;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (currentTokens >= 1) {
                currentTokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long delta = now - lastRefillTimestamp;
            double tokensToAdd = delta * (maxTokens / (double) TimeUnit.HOURS.toMillis(1));

            currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
            lastRefillTimestamp = now;
        }

        public long getTokens() {
            refill();
            return (long) currentTokens;
        }

        public long getSecondsToReset() {
            return TimeUnit.MILLISECONDS.toSeconds(lastRefillTimestamp + TimeUnit.HOURS.toMillis(1) - System.currentTimeMillis());
        }
    }

    private final Map<String, TokenBucket> clientLimits = new ConcurrentHashMap<>();
    private final long hourlyLimit = 1000;

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clientLimits.computeIfAbsent(clientId,
                k -> new TokenBucket(hourlyLimit, TimeUnit.HOURS.toMillis(1)));

        if (bucket.tryConsume()) {
            return "Allowed (" + bucket.getTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after " + bucket.getSecondsToReset() + "s)";
        }
    }

    public static void main(String[] args) {
        DistributedRateLimiter limiter = new DistributedRateLimiter();

        System.out.println("checkRateLimit(\"abc123\"): " + limiter.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(\"abc123\"): " + limiter.checkRateLimit("abc123"));

        for(int i = 0; i < 1000; i++) limiter.checkRateLimit("busy_client");
        System.out.println("checkRateLimit(\"busy_client\"): " + limiter.checkRateLimit("busy_client"));
    }
}