import java.util.*;

public class FinancialFraudDetector {
    static class Transaction {
        int id;
        int amount;
        String merchant;
        long timestamp;
        String accountId;

        Transaction(int id, int amount, String merchant, long timestamp, String accountId) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.timestamp = timestamp;
            this.accountId = accountId;
        }
    }

    public List<String> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> complements = new HashMap<>();
        List<String> pairs = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (complements.containsKey(complement)) {
                pairs.add("(id:" + complements.get(complement).id + ", id:" + t.id + ")");
            }
            complements.put(t.amount, t);
        }
        return pairs;
    }

    public List<String> findTwoSumWithTimeWindow(List<Transaction> transactions, int target, long windowMillis) {
        List<String> pairs = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t1 = transactions.get(i);
                Transaction t2 = transactions.get(j);

                if (Math.abs(t1.timestamp - t2.timestamp) <= windowMillis) {
                    if (t1.amount + t2.amount == target) {
                        pairs.add("(id:" + t1.id + ", id:" + t2.id + ")");
                    }
                }
            }
        }
        return pairs;
    }

    public List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<String>> merchantAmountMap = new HashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.merchant + "_" + t.amount;
            merchantAmountMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t.accountId);
        }

        for (Map.Entry<String, List<String>> entry : merchantAmountMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.add("{Pattern: " + entry.getKey() + ", accounts: " + entry.getValue() + "}");
            }
        }
        return duplicates;
    }

    public static void main(String[] args) {
        FinancialFraudDetector detector = new FinancialFraudDetector();
        List<Transaction> txns = Arrays.asList(
                new Transaction(1, 500, "Store A", 1000000, "acc1"),
                new Transaction(2, 300, "Store B", 1000500, "acc2"),
                new Transaction(3, 200, "Store C", 1001000, "acc3"),
                new Transaction(4, 500, "Store A", 1002000, "acc4")
        );

        System.out.println("findTwoSum(target=500): " + detector.findTwoSum(txns, 500));
        System.out.println("detectDuplicates(): " + detector.detectDuplicates(txns));
    }
}