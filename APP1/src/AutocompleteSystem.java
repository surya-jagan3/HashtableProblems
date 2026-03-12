import java.util.*;

public class AutocompleteSystem {
    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
    }

    private final TrieNode root;

    public AutocompleteSystem() {
        root = new TrieNode();
    }

    public void updateFrequency(String query) {
        TrieNode curr = root;
        for (char c : query.toCharArray()) {
            curr = curr.children.computeIfAbsent(c, k -> new TrieNode());
            curr.counts.put(query, curr.counts.getOrDefault(query, 0) + 1);
        }
    }

    public List<String> search(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            if (!curr.children.containsKey(c)) {
                return new ArrayList<>();
            }
            curr = curr.children.get(c);
        }

        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                (a, b) -> a.getValue().equals(b.getValue())
                        ? a.getKey().compareTo(b.getKey())
                        : b.getValue() - a.getValue()
        );

        pq.addAll(curr.counts.entrySet());

        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < 10 && !pq.isEmpty(); i++) {
            suggestions.add(pq.poll().getKey());
        }
        return suggestions;
    }

    public static void main(String[] args) {
        AutocompleteSystem engine = new AutocompleteSystem();

        engine.updateFrequency("java tutorial");
        engine.updateFrequency("java tutorial");
        engine.updateFrequency("javascript");
        engine.updateFrequency("java download");

        System.out.println("search(\"jav\"):");
        List<String> results = engine.search("jav");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }

        engine.updateFrequency("java 21 features");
        System.out.println("\nAfter trending update:");
        System.out.println(engine.search("java"));
    }
}