import java.util.*;

public class DNSCacheManager {
    class DNSEntry {
        String ipAddress;
        long expiryTime;

        DNSEntry(String ipAddress, int ttlSeconds) {
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private long hits = 0;
    private long misses = 0;

    public DNSCacheManager(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCacheManager.this.capacity;
            }
        };
    }

    public String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return entry.ipAddress + " (Cache HIT)";
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses++;
        String upstreamIp = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(upstreamIp, 300));
        return upstreamIp + " (Cache MISS/EXPIRED -> Query Upstream)";
    }

    private String queryUpstreamDNS(String domain) {
        return "172.217.14." + (new Random().nextInt(254) + 1);
    }

    public void getCacheStats() {
        double total = hits + misses;
        double hitRate = (total == 0) ? 0 : (hits / total) * 100;
        System.out.println("--- Cache Statistics ---");
        System.out.println("Hits: " + hits + " | Misses: " + misses);
        System.out.printf("Hit Rate: %.1f%%\n", hitRate);
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCacheManager dns = new DNSCacheManager(2);

        System.out.println(dns.resolve("google.com"));
        System.out.println(dns.resolve("google.com"));

        System.out.println(dns.resolve("example.com"));
        dns.getCacheStats();
    }
}