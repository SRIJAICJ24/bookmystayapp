import java.io.*;
import java.util.*;

// ---------------------------
// Reservation
// ---------------------------
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reservationId;
    private String guestName;
    private String roomType;
    private boolean active;

    public Reservation(String reservationId, String guestName, String roomType) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.active = true;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public boolean isActive() {
        return active;
    }

    public void cancel() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId +
                ", Guest: " + guestName +
                ", Room Type: " + roomType +
                ", Status: " + (active ? "CONFIRMED" : "CANCELLED");
    }
}

// ---------------------------
// Booking History
// ---------------------------
class BookingHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Reservation> reservations;

    public BookingHistory() {
        reservations = new ArrayList<>();
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void displayHistory() {
        System.out.println("\nBooking History:");
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Reservation reservation : reservations) {
            System.out.println(reservation);
        }
    }
}

// ---------------------------
// Inventory
// ---------------------------
class HotelInventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Integer> roomAvailability;

    public HotelInventory() {
        roomAvailability = new HashMap<>();
        roomAvailability.put("Standard", 3);
        roomAvailability.put("Deluxe", 2);
        roomAvailability.put("Suite", 1);
    }

    public boolean bookRoom(String roomType) {
        Integer available = roomAvailability.get(roomType);

        if (available == null) {
            System.out.println("Invalid room type: " + roomType);
            return false;
        }

        if (available <= 0) {
            System.out.println("No rooms available for type: " + roomType);
            return false;
        }

        roomAvailability.put(roomType, available - 1);
        return true;
    }

    public void restoreRoom(String roomType) {
        if (roomAvailability.containsKey(roomType)) {
            roomAvailability.put(roomType, roomAvailability.get(roomType) + 1);
        }
    }

    public Map<String, Integer> getRoomAvailability() {
        return Collections.unmodifiableMap(roomAvailability);
    }

    public void displayInventory() {
        System.out.println("\nCurrent Inventory:");
        for (Map.Entry<String, Integer> entry : roomAvailability.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}

// ---------------------------
// System State
// A wrapper object to persist
// both booking history and inventory together
// ---------------------------
class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;

    private BookingHistory bookingHistory;
    private HotelInventory inventory;
    private int reservationCounter;

    public SystemState(BookingHistory bookingHistory, HotelInventory inventory, int reservationCounter) {
        this.bookingHistory = bookingHistory;
        this.inventory = inventory;
        this.reservationCounter = reservationCounter;
    }

    public BookingHistory getBookingHistory() {
        return bookingHistory;
    }

    public HotelInventory getInventory() {
        return inventory;
    }

    public int getReservationCounter() {
        return reservationCounter;
    }
}

// ---------------------------
// Persistence Service
// ---------------------------
class PersistenceService {
    private final String fileName;

    public PersistenceService(String fileName) {
        this.fileName = fileName;
    }

    public void save(SystemState state) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(state);
            System.out.println("\nSystem state saved successfully to file: " + fileName);
        } catch (IOException e) {
            System.out.println("\nFailed to save system state: " + e.getMessage());
        }
    }

    public SystemState load() {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("\nPersistence file not found. Starting with a fresh system state.");
            return null;
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            Object obj = inputStream.readObject();

            if (obj instanceof SystemState) {
                System.out.println("\nSystem state restored successfully from file: " + fileName);
                return (SystemState) obj;
            } else {
                System.out.println("\nPersistence file format is invalid. Starting with a fresh system state.");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\nFailed to restore system state: " + e.getMessage());
            System.out.println("Starting with a fresh system state.");
            return null;
        }
    }
}

// ---------------------------
// Booking System
// ---------------------------
class BookingSystem {
    private BookingHistory bookingHistory;
    private HotelInventory inventory;
    private int reservationCounter;
    private PersistenceService persistenceService;

    public BookingSystem(String persistenceFile) {
        persistenceService = new PersistenceService(persistenceFile);
        restoreState();
    }

    private void restoreState() {
        SystemState loadedState = persistenceService.load();

        if (loadedState != null) {
            this.bookingHistory = loadedState.getBookingHistory();
            this.inventory = loadedState.getInventory();
            this.reservationCounter = loadedState.getReservationCounter();
        } else {
            this.bookingHistory = new BookingHistory();
            this.inventory = new HotelInventory();
            this.reservationCounter = 1;
        }
    }

    public void confirmBooking(String guestName, String roomType) {
        boolean booked = inventory.bookRoom(roomType);

        if (!booked) {
            System.out.println("Booking failed for guest: " + guestName);
            return;
        }

        String reservationId = "R" + reservationCounter++;
        Reservation reservation = new Reservation(reservationId, guestName, roomType);
        bookingHistory.addReservation(reservation);

        System.out.println("Booking confirmed: " + reservation);
    }

    public void shutdown() {
        SystemState state = new SystemState(bookingHistory, inventory, reservationCounter);
        persistenceService.save(state);
    }

    public void displaySystemState() {
        inventory.displayInventory();
        bookingHistory.displayHistory();
        System.out.println("\nNext Reservation Counter: " + reservationCounter);
    }
}

// ---------------------------
// Demo
// ---------------------------
public class PersistenceRecoveryDemo {
    public static void main(String[] args) {
        String fileName = "hotel_state.dat";

        System.out.println("=== SYSTEM STARTUP ===");
        BookingSystem system = new BookingSystem(fileName);

        system.displaySystemState();

        System.out.println("\n=== NEW OPERATIONS ===");
        system.confirmBooking("Sri", "Standard");
        system.confirmBooking("Kavi", "Deluxe");
        system.confirmBooking("Maya", "Suite");

        system.displaySystemState();

        System.out.println("\n=== SYSTEM SHUTDOWN ===");
        system.shutdown();

        System.out.println("\n======================================");
        System.out.println("Simulating application restart...");
        System.out.println("======================================");

        System.out.println("\n=== SYSTEM RESTART ===");
        BookingSystem restartedSystem = new BookingSystem(fileName);
        restartedSystem.displaySystemState();

        System.out.println("\n=== CONTINUING OPERATIONS AFTER RECOVERY ===");
        restartedSystem.confirmBooking("Arun", "Standard");
        restartedSystem.displaySystemState();

        System.out.println("\n=== FINAL SHUTDOWN ===");
        restartedSystem.shutdown();
    }
}
