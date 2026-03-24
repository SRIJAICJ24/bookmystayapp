import java.util.HashMap;
import java.util.Map;

class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;
    private int numberOfRooms;

    public Reservation(String reservationId, String guestName, String roomType, int numberOfRooms) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.numberOfRooms = numberOfRooms;
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

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId +
               ", Guest Name: " + guestName +
               ", Room Type: " + roomType +
               ", Number of Rooms: " + numberOfRooms;
    }
}

class InvalidBookingValidator {
    public static void validateBookingInput(String guestName, String roomType, int numberOfRooms,
                                            Map<String, Integer> inventory) throws InvalidBookingException {

        if (guestName == null || guestName.trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }

        if (roomType == null || roomType.trim().isEmpty()) {
            throw new InvalidBookingException("Room type cannot be empty.");
        }

        if (!inventory.containsKey(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }

        if (numberOfRooms <= 0) {
            throw new InvalidBookingException("Number of rooms must be greater than zero.");
        }

        int availableRooms = inventory.get(roomType);

        if (availableRooms < 0) {
            throw new InvalidBookingException("System error: inventory for room type '" + roomType + "' is invalid.");
        }

        if (numberOfRooms > availableRooms) {
            throw new InvalidBookingException(
                "Booking failed: only " + availableRooms + " room(s) available for room type '" + roomType + "'."
            );
        }
    }
}

class BookingSystem {
    private Map<String, Integer> inventory;
    private int reservationCounter;

    public BookingSystem() {
        inventory = new HashMap<>();
        reservationCounter = 1;

        inventory.put("Standard", 5);
        inventory.put("Deluxe", 3);
        inventory.put("Suite", 2);
    }

    public Reservation confirmBooking(String guestName, String roomType, int numberOfRooms)
            throws InvalidBookingException {

        InvalidBookingValidator.validateBookingInput(guestName, roomType, numberOfRooms, inventory);

        int availableRooms = inventory.get(roomType);

        if (availableRooms - numberOfRooms < 0) {
            throw new InvalidBookingException(
                "Booking would cause negative inventory for room type '" + roomType + "'."
            );
        }

        inventory.put(roomType, availableRooms - numberOfRooms);

        String reservationId = "R" + reservationCounter++;
        return new Reservation(reservationId, guestName, roomType, numberOfRooms);
    }

    public void displayInventory() {
        System.out.println("\nCurrent Inventory:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " room(s) available");
        }
    }
}

public class BookingValidationDemo {
    public static void main(String[] args) {
        BookingSystem bookingSystem = new BookingSystem();

        bookingSystem.displayInventory();

        processBooking(bookingSystem, "Sri", "Deluxe", 1);
        processBooking(bookingSystem, "Kavi", "Premium", 1);
        processBooking(bookingSystem, "Arun", "Suite", 5);
        processBooking(bookingSystem, "", "Standard", 1);
        processBooking(bookingSystem, "Maya", "Standard", -2);
        processBooking(bookingSystem, "John", "Standard", 2);

        bookingSystem.displayInventory();
    }

    public static void processBooking(BookingSystem bookingSystem, String guestName, String roomType, int numberOfRooms) {
        try {
            System.out.println("\nAttempting booking for " + guestName + "...");
            Reservation reservation = bookingSystem.confirmBooking(guestName, roomType, numberOfRooms);
            System.out.println("Booking successful!");
            System.out.println(reservation);
        } catch (InvalidBookingException e) {
            System.out.println("Booking error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected system error: " + e.getMessage());
        }
    }
}