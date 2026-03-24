import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

class BookingRequest {
    private String guestName;
    private String roomType;

    public BookingRequest(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return "Guest: " + guestName + ", Room Type: " + roomType;
    }
}

class SharedBookingQueue {
    private Queue<BookingRequest> queue = new LinkedList<>();

    public synchronized void addRequest(BookingRequest request) {
        queue.add(request);
        System.out.println(Thread.currentThread().getName() + " added request -> " + request);
    }

    public synchronized BookingRequest getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

class HotelInventory {
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Integer> allocationCounter = new HashMap<>();

    public HotelInventory() {
        inventory.put("Standard", 2);
        inventory.put("Deluxe", 2);
        inventory.put("Suite", 1);

        allocationCounter.put("Standard", 0);
        allocationCounter.put("Deluxe", 0);
        allocationCounter.put("Suite", 0);
    }

    public synchronized boolean allocateRoom(String guestName, String roomType) {
        Integer available = inventory.get(roomType);

        if (available == null) {
            System.out.println(Thread.currentThread().getName() + " -> Invalid room type for " + guestName + ": " + roomType);
            return false;
        }

        if (available <= 0) {
            System.out.println(Thread.currentThread().getName() + " -> No " + roomType + " rooms left for " + guestName);
            return false;
        }

        inventory.put(roomType, available - 1);
        allocationCounter.put(roomType, allocationCounter.get(roomType) + 1);

        System.out.println(Thread.currentThread().getName() +
                " -> Booking confirmed for " + guestName +
                " | Room Type: " + roomType +
                " | Remaining: " + inventory.get(roomType));

        return true;
    }

    public synchronized void displayInventory() {
        System.out.println("\nFinal Inventory State:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " -> Available: " + entry.getValue());
        }
    }

    public synchronized void displayAllocations() {
        System.out.println("\nAllocation Summary:");
        for (Map.Entry<String, Integer> entry : allocationCounter.entrySet()) {
            System.out.println(entry.getKey() + " -> Allocated: " + entry.getValue());
        }
    }
}

class ConcurrentBookingProcessor implements Runnable {
    private SharedBookingQueue bookingQueue;
    private HotelInventory inventory;

    public ConcurrentBookingProcessor(SharedBookingQueue bookingQueue, HotelInventory inventory) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        while (true) {
            BookingRequest request;

            synchronized (bookingQueue) {
                if (bookingQueue.isEmpty()) {
                    break;
                }
                request = bookingQueue.getNextRequest();
            }

            if (request != null) {
                inventory.allocateRoom(request.getGuestName(), request.getRoomType());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted.");
            }
        }
    }
}

public class ConcurrentBookingDemo {
    public static void main(String[] args) {
        SharedBookingQueue bookingQueue = new SharedBookingQueue();
        HotelInventory inventory = new HotelInventory();

        bookingQueue.addRequest(new BookingRequest("Sri", "Standard"));
        bookingQueue.addRequest(new BookingRequest("Kavi", "Standard"));
        bookingQueue.addRequest(new BookingRequest("Arun", "Standard"));
        bookingQueue.addRequest(new BookingRequest("Maya", "Deluxe"));
        bookingQueue.addRequest(new BookingRequest("John", "Deluxe"));
        bookingQueue.addRequest(new BookingRequest("Riya", "Suite"));
        bookingQueue.addRequest(new BookingRequest("Asha", "Suite"));

        Thread t1 = new Thread(new ConcurrentBookingProcessor(bookingQueue, inventory), "Processor-1");
        Thread t2 = new Thread(new ConcurrentBookingProcessor(bookingQueue, inventory), "Processor-2");
        Thread t3 = new Thread(new ConcurrentBookingProcessor(bookingQueue, inventory), "Processor-3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        }

        inventory.displayInventory();
        inventory.displayAllocations();
    }
}