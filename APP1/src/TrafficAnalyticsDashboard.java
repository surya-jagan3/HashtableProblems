import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TrafficAnalyticsDashboard {
    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    public void processEvent(String url, String userId, String source) {
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);

        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);

        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    public void getDashboard() {
        System.out.println("--- Real-Time Dashboard (Top 10 Pages) ---");

        List<Map.Entry<String, Integer>> topPages = pageViews.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int uniques = uniqueVisitors.get(url).size();
            System.out.printf("%d. %s - %d views (%d unique)%n", rank++, url, views, uniques);
        }

        System.out.println("\nTraffic Sources: " + trafficSources);
        System.out.println("------------------------------------------");
    }

    public static void main(String[] args) {
        TrafficAnalyticsDashboard dashboard = new TrafficAnalyticsDashboard();

        dashboard.processEvent("/article/breaking-news", "user_123", "google");
        dashboard.processEvent("/article/breaking-news", "user_456", "facebook");
        dashboard.processEvent("/sports/championship", "user_789", "google");
        dashboard.processEvent("/article/breaking-news", "user_123", "google");

        dashboard.getDashboard();
    }
}