import java.util.*;

public class PlagiarismDetector {
    private final Map<String, Set<String>> ngramIndex = new HashMap<>();
    private final Map<String, Integer> documentWordCounts = new HashMap<>();
    private static final int N = 5;

    public void indexDocument(String docId, String content) {
        List<String> ngrams = extractNGrams(content);
        documentWordCounts.put(docId, ngrams.size());

        for (String gram : ngrams) {
            ngramIndex.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    public void analyzeDocument(String newDocId, String content) {
        List<String> inputNgrams = extractNGrams(content);
        int totalInputNgrams = inputNgrams.size();
        Map<String, Integer> matchCounts = new HashMap<>();

        for (String gram : inputNgrams) {
            if (ngramIndex.containsKey(gram)) {
                for (String existingDocId : ngramIndex.get(gram)) {
                    matchCounts.put(existingDocId, matchCounts.getOrDefault(existingDocId, 0) + 1);
                }
            }
        }

        System.out.println("analyzeDocument(\"" + newDocId + "\")");
        System.out.println("-> Extracted " + totalInputNgrams + " n-grams");

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String existingDocId = entry.getKey();
            int matches = entry.getValue();
            double similarity = (matches * 100.0) / totalInputNgrams;

            String status = similarity > 50 ? "(PLAGIARISM DETECTED)" : (similarity > 10 ? "(suspicious)" : "");
            System.out.printf("-> Found %d matching n-grams with \"%s\"%n", matches, existingDocId);
            System.out.printf("-> Similarity: %.1f%% %s%n", similarity, status);
        }
    }

    private List<String> extractNGrams(String text) {
        List<String> grams = new ArrayList<>();
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(j < N - 1 ? " " : "");
            }
            grams.add(sb.toString());
        }
        return grams;
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        detector.indexDocument("essay_089.txt", "the quick brown fox jumps over the lazy dog");
        detector.indexDocument("essay_092.txt", "research shows that data structures are essential for modern computing systems");

        String newEssay = "research shows that data structures are critical for modern computing applications";
        detector.analyzeDocument("essay_123.txt", newEssay);
    }
}