import java.util.*;

public class UsernameAvailabilityChecker {
    private Map<String, Integer> userRegistry = new HashMap<>();
    private Map<String, Integer> attemptTracker = new HashMap<>();
    private static final int SUGGESTION_LIMIT = 3;

    public boolean checkAvailability(String username) {
        attemptTracker.put(username, attemptTracker.getOrDefault(username, 0) + 1);
        return !userRegistry.containsKey(username);
    }

    public void registerUser(String username, int userId) {
        userRegistry.put(username, userId);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        while (suggestions.size() < SUGGESTION_LIMIT) {
            String candidate = username + suffix;
            if (!userRegistry.containsKey(candidate)) {
                suggestions.add(candidate);
            }
            suffix++;
        }

        String dotCandidate = username.replace("_", ".");
        if (!username.equals(dotCandidate) && !userRegistry.containsKey(dotCandidate)) {
            suggestions.add(dotCandidate);
        }

        return suggestions;
    }

    public String getMostAttempted() {
        return Collections.max(attemptTracker.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static void main(String[] args) {
        UsernameAvailabilityChecker system = new UsernameAvailabilityChecker();

        system.registerUser("john_doe", 101);

        System.out.println("checkAvailability(\"john_doe\") -> " + system.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " + system.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") -> " + system.suggestAlternatives("john_doe"));

        system.checkAvailability("admin");
        system.checkAvailability("admin");
        System.out.println("getMostAttempted() -> " + system.getMostAttempted());
    }
}