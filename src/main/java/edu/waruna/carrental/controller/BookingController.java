package edu.waruna.carrental.controller;

import edu.waruna.carrental.dto.BookingDTO;
import edu.waruna.carrental.dto.CarDTO;
import edu.waruna.carrental.dto.UserDTO;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository; 

    // Place a booking
    @PostMapping
    public ResponseEntity<?> placeBooking(@RequestBody Booking booking) {

        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Booking dates cannot be empty!");
        }

        if (booking.getStartDate().isBefore(java.time.LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Start date cannot be in the past!");
        }

        if (booking.getEndDate().isBefore(booking.getStartDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: End date cannot be before start date!");
        }

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
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(b -> {
            BookingDTO dto = new BookingDTO();
            dto.setId(b.getId());
            dto.setStartDate(b.getStartDate());
            dto.setEndDate(b.getEndDate());
            dto.setTotalPrice(b.getTotalPrice());
            dto.setStatus(b.getStatus());
            dto.setPaymentStatus(b.getPaymentStatus());

            if (b.getCar() != null) {
                CarDTO carDto = new CarDTO();
                carDto.setId(b.getCar().getId());
                carDto.setBrand(b.getCar().getBrand());
                carDto.setModel(b.getCar().getModel());
                dto.setCar(carDto);
            }

            if (b.getUser() != null) {
                UserDTO userDto = new UserDTO(b.getUser().getId(), b.getUser().getName(), b.getUser().getEmail(), b.getUser().getRole());
                dto.setUser(userDto);
                dto.setCustomerName(b.getUser().getName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<BookingDTO> getBookingsByUserId(@PathVariable Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(b -> {
            BookingDTO dto = new BookingDTO();
            dto.setId(b.getId());
            dto.setStartDate(b.getStartDate());
            dto.setEndDate(b.getEndDate());
            dto.setTotalPrice(b.getTotalPrice());
            dto.setStatus(b.getStatus());
            dto.setPaymentStatus(b.getPaymentStatus());

            if (b.getCar() != null) {
                CarDTO carDto = new CarDTO();
                carDto.setId(b.getCar().getId());
                carDto.setBrand(b.getCar().getBrand());
                carDto.setModel(b.getCar().getModel());
                dto.setCar(carDto);
            }

            if (b.getUser() != null) {
                UserDTO userDto = new UserDTO(b.getUser().getId(), b.getUser().getName(), b.getUser().getEmail(), b.getUser().getRole());
                dto.setUser(userDto);
                dto.setCustomerName(b.getUser().getName());
            }

            return dto;
        }).collect(Collectors.toList());
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