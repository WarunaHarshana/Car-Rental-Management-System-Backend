package edu.waruna.carrental.controller;

import edu.waruna.carrental.entity.Booking;
import edu.waruna.carrental.entity.Car;
import edu.waruna.carrental.entity.User;
import edu.waruna.carrental.repository.BookingRepository;
import edu.waruna.carrental.repository.CarRepository;
import edu.waruna.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository; // 🚀 1. Inject your UserRepository here

    // Place a booking
    @PostMapping
    public ResponseEntity<?> placeBooking(@RequestBody Booking booking) {

        Optional<Car> carOpt = carRepository.findById(booking.getCar().getId());

        if (carOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Car not found!");
        }

        Car car = carOpt.get();
        if (!"AVAILABLE".equals(car.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: This car is currently rented or under maintenance!");
        }


        if (booking.getUser() == null || booking.getUser().getId() == null) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                booking.setUser(users.get(0));
            } else {
                User dummyUser = new User();
                dummyUser.setName("Test Customer");
                dummyUser.setEmail("test@drivexpress.com");
                dummyUser.setPassword("password123");
                userRepository.save(dummyUser);
                booking.setUser(dummyUser);
            }
        }

        // Calculate total price
        long rentalDays = java.time.temporal.ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (rentalDays <= 0) {
            rentalDays = 1;
        }
        double totalCost = rentalDays * car.getDailyPrice();
        booking.setTotalPrice(totalCost);

        booking.setStatus("PENDING");
        booking.setPaymentStatus("PENDING");

        car.setStatus("RENTED");
        carRepository.save(car);

        // Save the booking record
        Booking savedBooking = bookingRepository.save(booking);

        return ResponseEntity.ok(savedBooking);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsByUserId(@PathVariable Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Booking record not found!");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(status.toUpperCase());

        if ("APPROVED".equalsIgnoreCase(status)) {
            Car car = booking.getCar();
            car.setStatus("RENTED");
            carRepository.save(car);
        } else if ("CANCELLED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            Car car = booking.getCar();
            car.setStatus("AVAILABLE");
            carRepository.save(car);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(updatedBooking);
    }
}