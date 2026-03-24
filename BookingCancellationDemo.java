import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class InvalidCancellationException extends Exception {
    public InvalidCancellationException(String message) {
        super(message);
    }
}

class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;
    private String allocatedRoomId;
    private boolean active;

    public Reservation(String reservationId, String guestName, String roomType, String allocatedRoomId) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.allocatedRoomId = allocatedRoomId;
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

    public String getAllocatedRoomId() {
        return allocatedRoomId;
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
                ", Guest Name: " + guestName +
                ", Room Type: " + roomType +
                ", Allocated Room ID: " + allocatedRoomId +
                ", Status: " + (active ? "CONFIRMED" : "CANCELLED");
    }
}

class BookingHistory {
    private Map<String, Reservation> reservationMap;

    public BookingHistory() {
        reservationMap = new HashMap<>();
    }

    public void addReservation(Reservation reservation) {
        reservationMap.put(reservation.getReservationId(), reservation);
    }

    public Reservation getReservation(String reservationId) {
        return reservationMap.get(reservationId);
    }

    public void displayAllReservations() {
        if (reservationMap.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        System.out.println("\nBooking History:");
        for (Reservation reservation : reservationMap.values()) {
            System.out.println(reservation);
        }
    }
}

class HotelInventory {
    private Map<String, Integer> roomCount;
    private Map<String, Stack<String>> availableRoomIds;

    public HotelInventory() {
        roomCount = new HashMap<>();
        availableRoomIds = new HashMap<>();

        initializeRoomType("Standard", new String[]{"S101", "S102", "S103"});
        initializeRoomType("Deluxe", new String[]{"D201", "D202"});
        initializeRoomType("Suite", new String[]{"SU301"});
    }

    private void initializeRoomType(String roomType, String[] roomIds) {
        Stack<String> stack = new Stack<>();
        for (int i = roomIds.length - 1; i >= 0; i--) {
            stack.push(roomIds[i]);
        }
        availableRoomIds.put(roomType, stack);
        roomCount.put(roomType, roomIds.length);
    }

    public String allocateRoom(String roomType) throws Exception {
        if (!roomCount.containsKey(roomType)) {
            throw new Exception("Invalid room type: " + roomType);
        }

        if (roomCount.get(roomType) <= 0 || availableRoomIds.get(roomType).isEmpty()) {
            throw new Exception("No rooms available for room type: " + roomType);
        }

        roomCount.put(roomType, roomCount.get(roomType) - 1);
        return availableRoomIds.get(roomType).pop();
    }

    public void restoreRoom(String roomType, String roomId) throws InvalidCancellationException {
        if (!roomCount.containsKey(roomType) || !availableRoomIds.containsKey(roomType)) {
            throw new InvalidCancellationException("Cannot restore room. Invalid room type: " + roomType);
        }

        availableRoomIds.get(roomType).push(roomId);
        roomCount.put(roomType, roomCount.get(roomType) + 1);
    }

    public void displayInventory() {
        System.out.println("\nCurrent Inventory:");
        for (String roomType : roomCount.keySet()) {
            System.out.println(roomType + " -> Available Count: " + roomCount.get(roomType)
                    + ", Available Room IDs: " + availableRoomIds.get(roomType));
        }
    }
}

class BookingService {
    private HotelInventory inventory;
    private BookingHistory bookingHistory;
    private int reservationCounter;

    public BookingService(HotelInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
        this.reservationCounter = 1;
    }

    public Reservation confirmBooking(String guestName, String roomType) throws Exception {
        String roomId = inventory.allocateRoom(roomType);
        String reservationId = "R" + reservationCounter++;

        Reservation reservation = new Reservation(reservationId, guestName, roomType, roomId);
        bookingHistory.addReservation(reservation);

        return reservation;
    }
}

class CancellationService {
    private HotelInventory inventory;
    private BookingHistory bookingHistory;
    private Stack<String> rollbackReleasedRoomIds;

    public CancellationService(HotelInventory inventory, BookingHistory bookingHistory) {
        this.inventory = inventory;
        this.bookingHistory = bookingHistory;
        this.rollbackReleasedRoomIds = new Stack<>();
    }

    public void cancelReservation(String reservationId) throws InvalidCancellationException {
        Reservation reservation = bookingHistory.getReservation(reservationId);

        if (reservation == null) {
            throw new InvalidCancellationException("Cancellation failed: reservation ID '" + reservationId + "' does not exist.");
        }

        if (!reservation.isActive()) {
            throw new InvalidCancellationException("Cancellation failed: reservation ID '" + reservationId + "' is already cancelled.");
        }

        String roomId = reservation.getAllocatedRoomId();
        String roomType = reservation.getRoomType();

        rollbackReleasedRoomIds.push(roomId);

        inventory.restoreRoom(roomType, roomId);

        reservation.cancel();

        System.out.println("Cancellation successful for reservation ID: " + reservationId);
        System.out.println("Released Room ID recorded in rollback stack: " + rollbackReleasedRoomIds.peek());
    }

    public void displayRollbackStack() {
        System.out.println("\nRollback Stack (Recently Released Room IDs): " + rollbackReleasedRoomIds);
    }
}

public class BookingCancellationDemo {
    public static void main(String[] args) {
        HotelInventory inventory = new HotelInventory();
        BookingHistory bookingHistory = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, bookingHistory);
        CancellationService cancellationService = new CancellationService(inventory, bookingHistory);

        try {
            Reservation r1 = bookingService.confirmBooking("Sri", "Standard");
            Reservation r2 = bookingService.confirmBooking("Kavi", "Deluxe");

            System.out.println("Booking successful!");
            System.out.println(r1);
            System.out.println(r2);

            inventory.displayInventory();
            bookingHistory.displayAllReservations();

            System.out.println("\n--- Cancelling Reservation " + r1.getReservationId() + " ---");
            cancellationService.cancelReservation(r1.getReservationId());

            inventory.displayInventory();
            bookingHistory.displayAllReservations();
            cancellationService.displayRollbackStack();

            System.out.println("\n--- Trying duplicate cancellation ---");
            cancellationService.cancelReservation(r1.getReservationId());

        } catch (InvalidCancellationException e) {
            System.out.println("Cancellation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("System Error: " + e.getMessage());
        }

        try {
            System.out.println("\n--- Trying cancellation for non-existent reservation ---");
            cancellationService.cancelReservation("R999");
        } catch (InvalidCancellationException e) {
            System.out.println("Cancellation Error: " + e.getMessage());
        }
    }
}