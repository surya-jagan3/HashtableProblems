import java.util.*;

public class MultiLevelCacheSystem {
    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;
    private final int PROMOTION_THRESHOLD = 5;

    private final Map<String, String> l1Cache;
    private final Map<String, String> l2Cache;
    private final Map<String, Integer> accessCounts = new HashMap<>();

    private long l1Hits = 0, l2Hits = 0, l3Hits = 0, totalRequests = 0;

    public MultiLevelCacheSystem() {
        this.l1Cache = new LinkedHashMap<String, String>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L1_CAPACITY;
            }
        };

        this.l2Cache = new LinkedHashMap<String, String>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L2_CAPACITY;
            }
        };
    }

    public String getVideo(String videoId) {
        totalRequests++;

        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            return l1Cache.get(videoId) + " (L1 HIT)";
        }

        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            String data = l2Cache.get(videoId);
            updateAccessAndPromote(videoId, data);
            return data + " (L2 HIT)";
        }

        l3Hits++;
        String data = queryDatabase(videoId);
        l2Cache.put(videoId, data);
        updateAccessAndPromote(videoId, data);
        return data + " (L3 HIT)";
    }

    private void updateAccessAndPromote(String videoId, String data) {
        int count = accessCounts.getOrDefault(videoId, 0) + 1;
        accessCounts.put(videoId, count);

        if (count >= PROMOTION_THRESHOLD) {
            l1Cache.put(videoId, data);
            l2Cache.remove(videoId);
        }
    }

    private String queryDatabase(String videoId) {
        return "VideoData_for_" + videoId;
    }

    public void getStatistics() {
        System.out.println("--- Cache Statistics ---");
        System.out.printf("L1 Hit Rate: %.1f%%%n", (l1Hits * 100.0 / totalRequests));
        System.out.printf("L2 Hit Rate: %.1f%%%n", (l2Hits * 100.0 / totalRequests));
        System.out.printf("L3 Hit Rate: %.1f%%%n", (l3Hits * 100.0 / totalRequests));
        double avgTime = (l1Hits * 0.5 + l2Hits * 5.0 + l3Hits * 150.0) / totalRequests;
        System.out.printf("Overall Avg Time: %.2fms%n", avgTime);
    }

    public static void main(String[] args) {
        MultiLevelCacheSystem netflix = new MultiLevelCacheSystem();

        System.out.println(netflix.getVideo("video_123")); // L3 Hit
        for(int i=0; i<5; i++) netflix.getVideo("video_123"); // Promote to L1
        System.out.println(netflix.getVideo("video_123")); // L1 Hit

        netflix.getStatistics();
    }
}