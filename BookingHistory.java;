
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;
    private int numberOfNights;
    private double totalCost;

    public Reservation(String reservationId, String guestName, String roomType, int numberOfNights, double totalCost) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.numberOfNights = numberOfNights;
        this.totalCost = totalCost;
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

    public int getNumberOfNights() {
        return numberOfNights;
    }

    public double getTotalCost() {
        return totalCost;
    }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId +
               ", Guest Name: " + guestName +
               ", Room Type: " + roomType +
               ", Nights: " + numberOfNights +
               ", Total Cost: " + totalCost;
    }
}

class BookingHistory {
    private List<Reservation> confirmedBookings;

    public BookingHistory() {
        confirmedBookings = new ArrayList<>();
    }

    public void addConfirmedBooking(Reservation reservation) {
        confirmedBookings.add(reservation);
    }

    public List<Reservation> getAllBookings() {
        return Collections.unmodifiableList(confirmedBookings);
    }

    public void displayBookingHistory() {
        if (confirmedBookings.isEmpty()) {
            System.out.println("No confirmed bookings found.");
            return;
        }

        System.out.println("Booking History:");
        for (Reservation reservation : confirmedBookings) {
            System.out.println(reservation);
        }
    }
}

class BookingReportService {
    public void generateSummaryReport(BookingHistory bookingHistory) {
        List<Reservation> bookings = bookingHistory.getAllBookings();

        System.out.println("\n===== BOOKING SUMMARY REPORT =====");

        if (bookings.isEmpty()) {
            System.out.println("No booking data available.");
            return;
        }

        int totalBookings = bookings.size();
        int totalNights = 0;
        double totalRevenue = 0;

        for (Reservation reservation : bookings) {
            totalNights += reservation.getNumberOfNights();
            totalRevenue += reservation.getTotalCost();
        }

        System.out.println("Total Confirmed Bookings: " + totalBookings);
        System.out.println("Total Nights Booked: " + totalNights);
        System.out.println("Total Revenue: " + totalRevenue);
    }

    public void generateDetailedReport(BookingHistory bookingHistory) {
        List<Reservation> bookings = bookingHistory.getAllBookings();

        System.out.println("\n===== DETAILED BOOKING REPORT =====");

        if (bookings.isEmpty()) {
            System.out.println("No booking data available.");
            return;
        }

        for (Reservation reservation : bookings) {
            System.out.println(reservation);
        }
    }
}

public class BookingHistoryDemo {
    public static void main(String[] args) {
        Reservation r1 = new Reservation("R101", "Sri", "Deluxe", 2, 4000.0);
        Reservation r2 = new Reservation("R102", "Kavi", "Suite", 3, 7500.0);
        Reservation r3 = new Reservation("R103", "Arun", "Standard", 1, 1800.0);

        BookingHistory bookingHistory = new BookingHistory();

        bookingHistory.addConfirmedBooking(r1);
        bookingHistory.addConfirmedBooking(r2);
        bookingHistory.addConfirmedBooking(r3);

        bookingHistory.displayBookingHistory();

        BookingReportService reportService = new BookingReportService();
        reportService.generateSummaryReport(bookingHistory);
        reportService.generateDetailedReport(bookingHistory);
    }
}
