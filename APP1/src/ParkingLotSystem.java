import java.util.*;

public class ParkingLotSystem {
    enum Status { EMPTY, OCCUPIED, DELETED }

    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status = Status.EMPTY;

        ParkingSpot() {}
    }

    private final ParkingSpot[] spots;
    private final int capacity;
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int parkActions = 0;

    public ParkingLotSystem(int size) {
        this.capacity = size;
        this.spots = new ParkingSpot[size];
        for (int i = 0; i < size; i++) {
            spots[i] = new ParkingSpot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public String parkVehicle(String licensePlate) {
        if (occupiedCount >= capacity) return "Parking Lot Full";

        int preferredSpot = hash(licensePlate);
        int currentSpot = preferredSpot;
        int probes = 0;

        while (spots[currentSpot].status == Status.OCCUPIED) {
            currentSpot = (currentSpot + 1) % capacity;
            probes++;
        }

        spots[currentSpot].licensePlate = licensePlate;
        spots[currentSpot].entryTime = System.currentTimeMillis();
        spots[currentSpot].status = Status.OCCUPIED;

        occupiedCount++;
        totalProbes += probes;
        parkActions++;

        return "Assigned spot #" + currentSpot + " (" + probes + " probes)";
    }

    public String exitVehicle(String licensePlate) {
        int preferredSpot = hash(licensePlate);
        int currentSpot = preferredSpot;
        int checked = 0;

        while (checked < capacity) {
            if (spots[currentSpot].status == Status.EMPTY) break;

            if (spots[currentSpot].status == Status.OCCUPIED &&
                    spots[currentSpot].licensePlate.equals(licensePlate)) {

                long durationMillis = System.currentTimeMillis() - spots[currentSpot].entryTime;
                double hours = Math.max(1.0, durationMillis / 3600000.0);
                double fee = hours * 5.0; // $5 per hour

                spots[currentSpot].status = Status.DELETED;
                spots[currentSpot].licensePlate = null;
                occupiedCount--;

                return String.format("Spot #%d freed, Fee: $%.2f", currentSpot, fee);
            }
            currentSpot = (currentSpot + 1) % capacity;
            checked++;
        }
        return "Vehicle not found";
    }

    public void getStatistics() {
        double occupancy = (double) occupiedCount / capacity * 100;
        double avgProbes = parkActions == 0 ? 0 : (double) totalProbes / parkActions;
        System.out.println("--- Parking Statistics ---");
        System.out.printf("Occupancy: %.1f%%%n", occupancy);
        System.out.printf("Avg Probes per Park: %.2f%n", avgProbes);
    }

    public static void main(String[] args) {
        ParkingLotSystem lot = new ParkingLotSystem(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        System.out.println(lot.exitVehicle("ABC-1234"));
        lot.getStatistics();
    }
}